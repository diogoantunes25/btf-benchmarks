import argparse, json, collections, socket, os, ansible_runner, time, pickle, math
from execo import *
from execo_g5k import *

G5K_SSH_KEY = "/home/dantunes/.ssh/id_rsa"
G5K_USER = "dantunes"
TMP_DESC_FILE = "/tmp/temporary_desc"
SETTING_FILE = "./setting.json"
WAIT_TIME = 1000 # milliseconds
ANSIBLE_VERBOSE = False

def get_walltime(settings): 
    milliseconds = sum(map(lambda setting: get_wait_time(setting), get_setting_list(settings, settings["master"], settings["replicas"], settings["clients"])))
    milliseconds = milliseconds * 2 + 300000

    seconds = math.ceil(milliseconds/1000)

    minutes = seconds // 60
    seconds = seconds % 60

    hours = minutes // 60
    minutes = minutes % 60

    return f"{hours:02}:{minutes:02}:{seconds:02}"

def g5k_reserve_nodes(master, replicas, clients, walltime):
    sites = [master] + replicas + clients

    plan = get_planning()
    slots = compute_slots(plan, walltime)

    resources_wanted = ({'grid5000': len(sites)+1})
    resources_wanted.update(collections.Counter(sites))

    logger.info(f"finding slots that fit: {resources_wanted}")
    (start, end, _) = find_free_slot(slots, resources_wanted)

    logger.info(f"slots found at {oar.format_oar_date(start)}")

    job_specs = get_jobs_specs(collections.Counter(sites))

    jobid, _ = oargridsub(job_specs, start, walltime = end - start)

    if jobid:
        logger.info("reservation successful")
        wait_oar_job_start(jobid)
        logger.info(f"job {jobid} started")
        nodes = get_oargrid_job_nodes(jobid)
        ips = {"master": None, "replicas": [], "clients": []}
          
        try:
            for node in nodes:
                site = get_host_site(node)
                ip = socket.gethostbyname(node.address)
                if ips["master"] is None and master == site:
                    ips["master"] = ip
                    master = None
                elif site in replicas:
                    ips["replicas"].append(ip)
                    replicas.remove(site)
                elif site in clients:
                    ips["clients"].append(ip)
                    clients.remove(site)
                else:
                    raise Exception(f"node_reservation failed: non requested site found")

        except:
            oargriddel([jobid])

        return (jobid, ips)

    raise Exception(f"node_reservation failed: jobid is {jobid}")
   
def create_inventory(master, replicas, clients, sshkey, user, filename = "./playbooks/inventory.ini"):
    fh = open(filename, "w")

    def write_host(name, ip):
        fh.write(f"{name.strip()} ansible_host=\"{ip.strip()}\" ansible_user={user.strip()} ansible_ssh_private_key_file=\"{sshkey.strip()}\"\n")

    fh.write("[nodes]\n")
    fh.write("manager ansible_host='localhost'\n")
    write_host("master", master)
    for i, replica in enumerate(replicas):
        write_host(f"replica{i}\n", replica)

    for i, client in enumerate(clients):
        write_host(f"client{i}\n", client)

    fh.write("\n[managers]\n")
    fh.write("manager\n")

    fh.write("\n[masters]\n")
    fh.write("master\n")

    fh.write("\n[replicas]\n")
    for i, replica in enumerate(replicas):
        fh.write(f"replica{i}\n")

    fh.write("\n[clients]\n")
    for i, client in enumerate(clients):
        fh.write(f"client{i}\n")

def update_ansible_vars(setting_file, g5k):
    fh = open("./playbooks/vars.yml", "w")
    
    fh.write('home_dir: "${HOME}"\n')
    fh.write('default_install_dir: "{{ home_dir }}/devel/alea_benchmark"\n')
    fh.write('alea_image_name: "diogoantunes25/alea_benchmark"\n')
    fh.write(f'exp_number: {len([e for e in os.listdir("./experiments") if e[0] == "r"])}\n')
    fh.write(f'setting_file: "{os.path.abspath(setting_file)}"\n')
    fh.write(f'description_file: "{os.path.abspath(TMP_DESC_FILE)}"\n')
    fh.write(f'g5k: {str(g5k).lower()}\n')

def get_setting_list(settings, master, replicas, clients):
    ans = []
    for section in settings["settings"]:
        for batch in section["batch"]:
            for protocol in section["protocol"]:
                for load in section["load"]:
                    for fault_mode in section["fault-mode"]:
                        for benchmarking_mode in section["benchmarking-mode"]:
                            n1 = int(section["n_replicas"])
                            n2 = int(section["n_clients"])
                            ans.append({ "batch": batch, "load": load, "protocol": protocol, "fault-mode": fault_mode, "benchmarking-mode": benchmarking_mode, "master": master, "replicas": replicas[:n1], "clients": clients[:n2], "duration": settings["duration"]})

    return ans


def run_playbook(name):
    playbook = os.path.abspath(f"./playbooks/{name}.yml")
    inventory = os.path.abspath(f"./playbooks/inventory.ini")
    runner = ansible_runner.interface.run(playbook = playbook, inventory = inventory, quiet = not ANSIBLE_VERBOSE)
    if len(runner.stats["failures"]) != 0:
        raise Exception("Playbook failed")

    return

def get_wait_time(setting):
    return WAIT_TIME * (2 * len(setting["replicas"]) + len(setting["clients"]) + 10) + setting["duration"]

def create_description_file(filename, settings, master, replicas, clients):
    """
    Creates the experiment description file and saves it to filename.
    The description file includes:
        - settings' description
        - setting of each run 
    """

    fh = open(filename, "w")

    desc = {
        "description": settings["description"],
        "runs": get_setting_list(settings, master, replicas, clients)
    }

    fh.write(json.dumps(desc)) 

def setup(settings_filename):
    # High-level check of json
    settings_file = open(settings_filename, "r")
    settings = json.loads(settings_file.read())
    for prop in ["description", "g5k", "replicas", "clients", "master", "duration", "settings"]:
        try:
            assert prop in settings
        except:
            print(f"{prop} not in settings")
            exit(1)

    jobid = None
    if settings["g5k"]:
        logger.info("g5k on - making node reservations")
        try:
            walltime = get_walltime(settings)
            logger.info(f"walltime is {walltime}")
            (jobid, ips) = g5k_reserve_nodes(settings["master"], settings["replicas"], settings["clients"], walltime)
            master = ips["master"]
            replicas = ips["replicas"]
            clients = ips["clients"]
            sshkey = G5K_SSH_KEY
            user = G5K_USER
        except:
            logger.info("node reservation failed")
    else:
        master = settings["master"]
        replicas = settings["replicas"]
        clients = settings["clients"]
        sshkey = settings["sshkey"]
        user = settings["user"]

    try:
        create_inventory(master, replicas, clients, sshkey, user)

        update_ansible_vars(SETTING_FILE, settings["g5k"])

        return [settings, master, replicas, clients, jobid]
    except:
        if settings["g5k"]: oargriddel([jobid])

def run(settings, master, replicas, clients, jobid):
    try:

        create_description_file(TMP_DESC_FILE, settings, master, replicas, clients)

        run_playbook("provision")
        print("Provisioning done.")

        for setting in get_setting_list(settings, master, replicas, clients):
            print(f"Benchmarking with n={len(setting['replicas'])}, batch={setting['batch']}, load={setting['load']} and protocol={setting['protocol']}.", end = " ")

            fh = open(SETTING_FILE, "w")
            fh.write(json.dumps(setting))
            fh.flush()
            fh.close()

            run_playbook("start")

            wait_time = get_wait_time(setting) / 1000
            print(f"Predicted to take {wait_time} seconds...", end = " ")
            time.sleep(wait_time)

            run_playbook("stop")
            print("Done")

        run_playbook("clean")
        print("Cleaning done.")
    finally:
        if settings["g5k"]: oargriddel([jobid])

    print("Experiments ended.")

def main():
    parser = argparse.ArgumentParser(description='Launch set of experiments')
    parser.add_argument("vars")
    parser.add_argument("--setup", action="store_true")
    parser.add_argument("--settings")
    parser.add_argument("--run", action="store_true")

    args = vars(parser.parse_args())

    if args["setup"]:
        if not args["settings"]: raise Exception("In setup mode, settings file is required")
        print("setup in progres...")
        data = setup(args["settings"])
        print("setup done")
        print(f"saving vars to {args['vars']}")
        with open(args["vars"], "wb") as fh:
            pickle.dump(data, fh)

    elif args["run"]:
        print(f"loading vars from {args['vars']}")
        with open(args["vars"], "rb") as fh:
            [settings, master, replicas, clients, jobid] = pickle.load(fh)

            print(f"running experiments")
            run(settings, master, replicas, clients, jobid)

    else:
        print("Error: either setup or run must be specified")

if __name__ == "__main__":
    main()
