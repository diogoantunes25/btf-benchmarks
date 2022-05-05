package pt.ulisboa.tecnico.thesis.benchmarks.contract;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.40.1)",
    comments = "Source: HeartbeatService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class HeartbeatServiceGrpc {

  private HeartbeatServiceGrpc() {}

  public static final String SERVICE_NAME = "pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse> getHeartbeatMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "heartbeat",
      requestType = pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest.class,
      responseType = pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest,
      pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse> getHeartbeatMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse> getHeartbeatMethod;
    if ((getHeartbeatMethod = HeartbeatServiceGrpc.getHeartbeatMethod) == null) {
      synchronized (HeartbeatServiceGrpc.class) {
        if ((getHeartbeatMethod = HeartbeatServiceGrpc.getHeartbeatMethod) == null) {
          HeartbeatServiceGrpc.getHeartbeatMethod = getHeartbeatMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest, pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "heartbeat"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse.getDefaultInstance()))
              .setSchemaDescriptor(new HeartbeatServiceMethodDescriptorSupplier("heartbeat"))
              .build();
        }
      }
    }
    return getHeartbeatMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static HeartbeatServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeartbeatServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeartbeatServiceStub>() {
        @java.lang.Override
        public HeartbeatServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeartbeatServiceStub(channel, callOptions);
        }
      };
    return HeartbeatServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static HeartbeatServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeartbeatServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeartbeatServiceBlockingStub>() {
        @java.lang.Override
        public HeartbeatServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeartbeatServiceBlockingStub(channel, callOptions);
        }
      };
    return HeartbeatServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static HeartbeatServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<HeartbeatServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<HeartbeatServiceFutureStub>() {
        @java.lang.Override
        public HeartbeatServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new HeartbeatServiceFutureStub(channel, callOptions);
        }
      };
    return HeartbeatServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class HeartbeatServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void heartbeat(pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getHeartbeatMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getHeartbeatMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest,
                pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse>(
                  this, METHODID_HEARTBEAT)))
          .build();
    }
  }

  /**
   */
  public static final class HeartbeatServiceStub extends io.grpc.stub.AbstractAsyncStub<HeartbeatServiceStub> {
    private HeartbeatServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeartbeatServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeartbeatServiceStub(channel, callOptions);
    }

    /**
     */
    public void heartbeat(pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getHeartbeatMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class HeartbeatServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<HeartbeatServiceBlockingStub> {
    private HeartbeatServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeartbeatServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeartbeatServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse heartbeat(pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getHeartbeatMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class HeartbeatServiceFutureStub extends io.grpc.stub.AbstractFutureStub<HeartbeatServiceFutureStub> {
    private HeartbeatServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HeartbeatServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new HeartbeatServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse> heartbeat(
        pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getHeartbeatMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_HEARTBEAT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final HeartbeatServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(HeartbeatServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_HEARTBEAT:
          serviceImpl.heartbeat((pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.HeartbeatResponse>) responseObserver);
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

  private static abstract class HeartbeatServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    HeartbeatServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.ulisboa.tecnico.thesis.benchmarks.contract.HeartbeatServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("HeartbeatService");
    }
  }

  private static final class HeartbeatServiceFileDescriptorSupplier
      extends HeartbeatServiceBaseDescriptorSupplier {
    HeartbeatServiceFileDescriptorSupplier() {}
  }

  private static final class HeartbeatServiceMethodDescriptorSupplier
      extends HeartbeatServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    HeartbeatServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (HeartbeatServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new HeartbeatServiceFileDescriptorSupplier())
              .addMethod(getHeartbeatMethod())
              .build();
        }
      }
    }
    return result;
  }
}