import argparse, json, collections, socket, os, ansible_runner, time
from execo import *
from execo_g5k import *

G5K_SSH_KEY = "/home/dantunes/.ssh/id_rsa"
G5K_USER = "dantunes"
SETTING_FILE = "./setting.json"
WAIT_TIME = 1000 # milliseconds
ANSIBLE_VERBOSE = False

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

def setup():
    parser = argparse.ArgumentParser(description='Launch set of experiments')
    parser.add_argument("settings")

    args = vars(parser.parse_args())

    # High-level check of json
    settings_file = open(args["settings"], "r")
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
            (jobid, ips) = g5k_reserve_nodes(settings["master"], settings["replicas"], settings["clients"], "0:05:00")
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

    finally:
        if settings["g5k"]: oargriddel([jobid])

    return [settings, master, replicas, clients, jobid]


def run(settings, master, replicas, clients, jobid = None):
    try:
        run_playbook("provision")
        print("provisioning done")

        for setting in get_setting_list(settings, master, replicas, clients):
            fh = open(SETTING_FILE, "w")
            fh.write(json.dumps(setting))
            fh.flush()
            fh.close()

            run_playbook("start")

            wait_time = get_wait_time(setting) / 1000
            print()
            print("==============================================")
            print(f"=SLEEPING FOR {wait_time} SECONDS====================")
            print("==============================================")
            time.sleep(wait_time)

            run_playbook("stop")

        run_playbook("clean")
        print("cleaning done")
    finally:
        if settings["g5k"]: oargriddel([jobid])

def main():
    print("setup in progres...")
    [settings, master, replicas, clients, jobid] = setup()
    print("setup done")
    run(settings, master, replicas, clients, jobid = jobid)

if __name__ == "__main__":
    main()
