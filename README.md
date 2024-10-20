## Architecture of the Application ##
This web server application is designed with a simple, modular, and multithreaded architecture. Below is a breakdown of the main 
componentsand how they interact with each other:  

### 1. Main Class (Main) ###  
- Responsibility: The entry point of the application where the server instance is created, routes are defined, and the server is started.  
Process:  
- It creates an instance of Server, specifying the port and thread pool size.  
- Registers routes and their corresponding HTTP methods (e.g., GET, POST) by attaching handlers to specific paths.  
- Starts the server to listen for incoming requests.  

### 2. Server Class (Server) ###  
- Responsibility: This is the core of the application, managing incoming client connections and routing the requests to the appropriate handlers.  
Process:  
- Port Binding: Opens a socket to listen for incoming connections on a specified port.  
- Thread Pool: Manages a fixed thread pool, allowing the server to handle multiple client connections concurrently, improving performance and scalability.  
- Request Handling: for each incoming connection, the server:      
- Parses the request using RequestParser.  
- Finds the appropriate handler based on the HTTP method and path (e.g., /messages for GET or POST).  
- Sends back the appropriate HTTP response.  
Concurrency: 
- The server leverages ExecutorService to handle multiple connections in parallel, enabling scalability for high traffic.  

### 3. Request Handling (Handler Interface) ###
- Responsibility: Abstract interface for implementing custom logic when handling specific routes. Each HTTP request path is associated with a specific 
handler that contains the business logic for that route.  
Process:   
- handle(request, outputStream) method is invoked for processing incoming requests based on the assigned route.  
Examples of handlers:  
- Handling GET requests to /messages.  
- Handling POST requests to /messages.  
- Returning error responses for unsupported paths or methods.  

### 4. Request Parsing (RequestParser) ###
- Responsibility: Parses raw HTTP requests (from BufferedInputStream) into structured Request objects.  
Process:  
- Request Line Parsing: Extracts the HTTP method (GET, POST), URL path, and HTTP version.  
- Header Parsing: Extracts key-value pairs from HTTP headers.  
- Body Parsing: For POST requests, the request body is processed based on the Content-Length header. If it's a form submission (application/x-www-form-urlencoded),
 it parses form parameters.  
- Query and POST Parameters: Splits query string or POST body data into key-value pairs for easy access in the Request object.  

### 5. Request Class (Request) ###
- Responsibility: Represents a structured HTTP request, including:  
- HTTP method (GET, POST).  
- Request path.  
- Query parameters (for GET requests).  
- POST parameters (for form data).  
- Headers and body.  
Process:  
- Used internally by handlers to access request data and parameters.    

### 6. Response Handling (Response) ###   
- Responsibility: Constructs and sends HTTP responses back to the client.  
Process:  
- Formats HTTP response headers (e.g., status code, content length, connection closure).  
- Sends pre-defined responses for common status codes like 200 (OK), 404 (Not Found), 500 (Internal Server Error), etc.  
- Reusability: Includes utility methods to handle various types of responses and errors, such as sendResponse, notFound, methodNotAllowed, and more.  
- Customization: Supports sending responses with different HTTP status codes and headers.  

### 7. Logging (via Logger) ###
- Responsibility: Logs server activity, including errors and status messages, for monitoring and debugging purposes.  
Process:
- The server writes log messages to a file (server.log).  
- Logs key actions like connection handling errors, request parsing issues, and internal server errors for later analysis.  

### 8. Multithreading ###
The server is designed to be multithreaded by using a thread pool. Each incoming request is processed in its own thread, allowing the server   
to handle multiple connections simultaneously without blocking.  
Concurrency Control: Classes like ConcurrentHashMap are used to manage routes and handlers in a thread-safe manner.  

### 9. Dynamic Route Management ###  
- Handlers Registration: The server allows dynamic registration of request handlers (e.g., for different HTTP methods like GET or POST and paths like /messages).  
- Routing: The Server class maintains a ConcurrentHashMap of HTTP methods and paths to route incoming requests to the appropriate handler.  
Example:
- server.addHandler("GET", "/messages", handler) registers a handler for GET requests on the /messages path.    

### Flow of an HTTP Request: ###  
- Client Sends Request: A client (e.g., browser or API client) sends an HTTP request to the server (e.g., GET /messages).  
- Server Accepts Connection: The ServerSocket in the Server class accepts the connection and hands off the request to a worker thread from the thread pool.  
- Request Parsing: The raw HTTP request is read by RequestParser and converted into a Request object.  
- Routing: The Server checks the HTTP method and path, and forwards the request to the appropriate Handler.  
- Business Logic Execution: The Handler processes the request (e.g., retrieves data, applies business logic).  
- Response Creation: A response is created using the Response class, formatted as an HTTP response.  
- Client Receives Response: The server sends the response back to the client and closes the connection.   

### Potential Improvements: ###  

- Route Parameter Support: Add support for dynamic route parameters (e.g., /messages/{id}).  
- Middleware: Add support for middleware (e.g., authentication, logging) that can be applied globally or on specific routes.  
- HTTP/2 Support: Extend the server to handle HTTP/2 for better performance (multiplexing, header compression).  
- Error Handling: Implement more robust error handling with detailed logging for different HTTP status codes.  
- Load Balancing: Add the capability to integrate with load balancers for scaling in production environments.  
- This modular architecture allows for flexibility, scalability, and easy extension of the server with additional features as needed.  