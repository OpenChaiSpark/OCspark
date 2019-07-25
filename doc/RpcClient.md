# RPC Client:  Native in Scala and coming soon to theatre’s near you: Java

## RPC Client Features 

The _RPC client_ is a _JVM_ library for providing a simple RPC mechanism supporting:
 
  - Separation of Control channels vs Data Channels
  - Control Channels:
    - Message based - as opposed to byte/frame based. This allows
    better usability and end-application reliability and simplicity
    - Request/Response mechanism:
        Send a request message and then receive a response message
  - Data Channels
    - Tcp Data Channel can use either NIO/ByteBuffer or simple Strings to transfer data
    - PcieDMA Data Channel transfers data via JNI to an externally generated C library to perform optimized data transfer
       - The JNI stubs and loopback for all API's are pre-integrated: so we just need the actual C _implementation_ of the API's to be provided
    - All data channels are just that - _data_ .. The setup and handshaking are done out-of-band within a separate (TCP) Control Channel
  - Fault/error _reporting_ at the message level. I.e an application may embed a Return Code in the Response indicating an Application fault
  - Fault/error _detecting_ at the transport level. Tcp channel errors on connections or on data transfers result in JVM Exceptions being thrown that bubble all the way up to the Server Console in the case of Server Side errors or to the API Client on the client side. The API Client application code must decide what to do in case of transport errors.  Note that this can be a point of improvement: ie. the Client API might be enhanced for fault tolerant behavior such as retry and/or reconnect to a different master. The latter however requires multi-master that is not currently supported. 
  - Definition of App-specific RPC Interfaces   
    - These custom interfaces define the handshakes between client and server
    - Each method in the API's follows a Request/Response format
      - Custom fields and structs may be defined: they simply _extend_ a base class of the Request/Response

## Verification of the RPC Client Features

The RPC client features are verified by a small set of unit and integration tests. These include:

 - Control channel verification 
 - Tcp Data channel verification 
 - Mixed control channel + Data channel verification 
 - Pcie Data channel JNI loopback verification 
 - Application error reporting: embed an error in the Response
 - Channel Error detection:  connection failed vs connectivity lost mid-message processing
 - App-specific RPC Interface validation: run a HelloWorld app via the rpc
   

## Appendix: Outline of ALL relevant available "pieces" from OCspark  
 
Let's decide which pieces we want

• RpcClient (aka TcpClient):  provides point to point  Messaging channel over Tcp.   YES.
•        e.g.  serialization/deserialization, packing, md5 checking
•        NIO/Byte buffer based data transfer over Tcp
•        In-process queueing of messages for flow control
•        Clear roles and support for Control channel vs Data channel
•        file data handling including multithreaded data read/write
* TFClient (aka TF):  Image File Submission and labeling service
*      Managing sets of images that are submitted to Resource Mgr to be processed by GPU's (aka TX's)
*      Fault tolerance and Failover included
*      Includes registry mechanism for async registration of Master and Workers
*      Includes support for both *DMA* and Tcp data channels
* _Not_ likely needed:
*            `P2PRdd`:  Supports out-of-band data and control channels for Spark.
*                  `SolverRDD`:   Distributed learning with  parameter server updates for Spark
*             `LocalitySensitiveRDD`:   RDD that "stays put" on a given worker. We wanted this for GPU locality
* _Maybe_ useful?   `DMA Client`
*       Drop in replacement for the `tcp` based data channel
*       Has Full JNI stubs (edited) 

javadba [5:12 AM]
The `RpcClient` is the core of it and we want it . We can chat on nailing down specific "wants" you have from it and a small set of deliverables that i'll do for this week.
The `TFClient` should at least be glanced at to see whether parts of it can be touched up to have some use
LIkewise for the `DMAClient`: it does already have JNI stubs and is fully integrated into `RpcClient` afaicr (edited) 
