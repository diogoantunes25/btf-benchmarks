package pt.ulisboa.tecnico.thesis.benchmarks.contract;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.40.1)",
    comments = "Source: BenchmarkService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class BenchmarkServiceGrpc {

  private BenchmarkServiceGrpc() {}

  public static final String SERVICE_NAME = "pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse> getTopologyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "topology",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse> getTopologyMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse> getTopologyMethod;
    if ((getTopologyMethod = BenchmarkServiceGrpc.getTopologyMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getTopologyMethod = BenchmarkServiceGrpc.getTopologyMethod) == null) {
          BenchmarkServiceGrpc.getTopologyMethod = getTopologyMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "topology"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("topology"))
              .build();
        }
      }
    }
    return getTopologyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse> getProtocolMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "protocol",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse> getProtocolMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse> getProtocolMethod;
    if ((getProtocolMethod = BenchmarkServiceGrpc.getProtocolMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getProtocolMethod = BenchmarkServiceGrpc.getProtocolMethod) == null) {
          BenchmarkServiceGrpc.getProtocolMethod = getProtocolMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "protocol"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("protocol"))
              .build();
        }
      }
    }
    return getProtocolMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse> getStartMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "start",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse> getStartMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse> getStartMethod;
    if ((getStartMethod = BenchmarkServiceGrpc.getStartMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getStartMethod = BenchmarkServiceGrpc.getStartMethod) == null) {
          BenchmarkServiceGrpc.getStartMethod = getStartMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "start"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("start"))
              .build();
        }
      }
    }
    return getStartMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse> getStopMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "stop",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse> getStopMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse> getStopMethod;
    if ((getStopMethod = BenchmarkServiceGrpc.getStopMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getStopMethod = BenchmarkServiceGrpc.getStopMethod) == null) {
          BenchmarkServiceGrpc.getStopMethod = getStopMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "stop"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("stop"))
              .build();
        }
      }
    }
    return getStopMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse> getInformMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "inform",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse> getInformMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse> getInformMethod;
    if ((getInformMethod = BenchmarkServiceGrpc.getInformMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getInformMethod = BenchmarkServiceGrpc.getInformMethod) == null) {
          BenchmarkServiceGrpc.getInformMethod = getInformMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "inform"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("inform"))
              .build();
        }
      }
    }
    return getInformMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse> getExecuteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "execute",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse> getExecuteMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse> getExecuteMethod;
    if ((getExecuteMethod = BenchmarkServiceGrpc.getExecuteMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getExecuteMethod = BenchmarkServiceGrpc.getExecuteMethod) == null) {
          BenchmarkServiceGrpc.getExecuteMethod = getExecuteMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "execute"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("execute"))
              .build();
        }
      }
    }
    return getExecuteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse> getShutdownMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "shutdown",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse> getShutdownMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse> getShutdownMethod;
    if ((getShutdownMethod = BenchmarkServiceGrpc.getShutdownMethod) == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        if ((getShutdownMethod = BenchmarkServiceGrpc.getShutdownMethod) == null) {
          BenchmarkServiceGrpc.getShutdownMethod = getShutdownMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "shutdown"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BenchmarkServiceMethodDescriptorSupplier("shutdown"))
              .build();
        }
      }
    }
    return getShutdownMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BenchmarkServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BenchmarkServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BenchmarkServiceStub>() {
        @java.lang.Override
        public BenchmarkServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BenchmarkServiceStub(channel, callOptions);
        }
      };
    return BenchmarkServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BenchmarkServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BenchmarkServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BenchmarkServiceBlockingStub>() {
        @java.lang.Override
        public BenchmarkServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BenchmarkServiceBlockingStub(channel, callOptions);
        }
      };
    return BenchmarkServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BenchmarkServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BenchmarkServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BenchmarkServiceFutureStub>() {
        @java.lang.Override
        public BenchmarkServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BenchmarkServiceFutureStub(channel, callOptions);
        }
      };
    return BenchmarkServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class BenchmarkServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * benchmark config
     * </pre>
     */
    public void topology(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTopologyMethod(), responseObserver);
    }

    /**
     */
    public void protocol(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProtocolMethod(), responseObserver);
    }

    /**
     * <pre>
     * benchmark control
     * </pre>
     */
    public void start(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStartMethod(), responseObserver);
    }

    /**
     */
    public void stop(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStopMethod(), responseObserver);
    }

    /**
     */
    public void inform(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInformMethod(), responseObserver);
    }

    /**
     * <pre>
     * others... TODO delete?
     * </pre>
     */
    public void execute(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecuteMethod(), responseObserver);
    }

    /**
     */
    public void shutdown(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getShutdownMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getTopologyMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse>(
                  this, METHODID_TOPOLOGY)))
          .addMethod(
            getProtocolMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse>(
                  this, METHODID_PROTOCOL)))
          .addMethod(
            getStartMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse>(
                  this, METHODID_START)))
          .addMethod(
            getStopMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse>(
                  this, METHODID_STOP)))
          .addMethod(
            getInformMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse>(
                  this, METHODID_INFORM)))
          .addMethod(
            getExecuteMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse>(
                  this, METHODID_EXECUTE)))
          .addMethod(
            getShutdownMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse>(
                  this, METHODID_SHUTDOWN)))
          .build();
    }
  }

  /**
   */
  public static final class BenchmarkServiceStub extends io.grpc.stub.AbstractAsyncStub<BenchmarkServiceStub> {
    private BenchmarkServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BenchmarkServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BenchmarkServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * benchmark config
     * </pre>
     */
    public void topology(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTopologyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void protocol(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProtocolMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * benchmark control
     * </pre>
     */
    public void start(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStartMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stop(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStopMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void inform(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInformMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * others... TODO delete?
     * </pre>
     */
    public void execute(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExecuteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void shutdown(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getShutdownMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BenchmarkServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<BenchmarkServiceBlockingStub> {
    private BenchmarkServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BenchmarkServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BenchmarkServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * benchmark config
     * </pre>
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse topology(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTopologyMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse protocol(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProtocolMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * benchmark control
     * </pre>
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse start(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStartMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse stop(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStopMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse inform(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInformMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * others... TODO delete?
     * </pre>
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse execute(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExecuteMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse shutdown(pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getShutdownMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BenchmarkServiceFutureStub extends io.grpc.stub.AbstractFutureStub<BenchmarkServiceFutureStub> {
    private BenchmarkServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BenchmarkServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BenchmarkServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * benchmark config
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse> topology(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTopologyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse> protocol(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProtocolMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * benchmark control
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse> start(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStartMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse> stop(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStopMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse> inform(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInformMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * others... TODO delete?
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse> execute(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExecuteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse> shutdown(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getShutdownMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_TOPOLOGY = 0;
  private static final int METHODID_PROTOCOL = 1;
  private static final int METHODID_START = 2;
  private static final int METHODID_STOP = 3;
  private static final int METHODID_INFORM = 4;
  private static final int METHODID_EXECUTE = 5;
  private static final int METHODID_SHUTDOWN = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BenchmarkServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BenchmarkServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TOPOLOGY:
          serviceImpl.topology((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.TopologyResponse>) responseObserver);
          break;
        case METHODID_PROTOCOL:
          serviceImpl.protocol((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ProtocolResponse>) responseObserver);
          break;
        case METHODID_START:
          serviceImpl.start((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StartBenchmarkResponse>) responseObserver);
          break;
        case METHODID_STOP:
          serviceImpl.stop((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.StopBenchmarkResponse>) responseObserver);
          break;
        case METHODID_INFORM:
          serviceImpl.inform((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.InformResponse>) responseObserver);
          break;
        case METHODID_EXECUTE:
          serviceImpl.execute((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.BenchmarkResponse>) responseObserver);
          break;
        case METHODID_SHUTDOWN:
          serviceImpl.shutdown((pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.ShutdownResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class BenchmarkServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BenchmarkServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BenchmarkService");
    }
  }

  private static final class BenchmarkServiceFileDescriptorSupplier
      extends BenchmarkServiceBaseDescriptorSupplier {
    BenchmarkServiceFileDescriptorSupplier() {}
  }

  private static final class BenchmarkServiceMethodDescriptorSupplier
      extends BenchmarkServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BenchmarkServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BenchmarkServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BenchmarkServiceFileDescriptorSupplier())
              .addMethod(getTopologyMethod())
              .addMethod(getProtocolMethod())
              .addMethod(getStartMethod())
              .addMethod(getStopMethod())
              .addMethod(getInformMethod())
              .addMethod(getExecuteMethod())
              .addMethod(getShutdownMethod())
              .build();
        }
      }
    }
    return result;
  }
}
