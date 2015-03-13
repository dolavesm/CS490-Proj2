import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
 
class ChatClient extends ReliableBroadcast implements Runnable, BroadcastReceiver, Message{
     
  @Override
  public void receive(Message m){
    ;
  }
  @Override
  public int getMessageNumber(){
    int i = 0;
    return i;
  }
  @Override
  public void setMessageNumber(int messageNumber){
    
  }
  @Override
  public String getMessageContents(){
    String j = "";
    return j;
  }
  @Override
  public void setMessageContents(String contents){
    
  }
    private String _name, _ip;
    private int _port;
    ArrayList<String> group = null;
    
    //Connection to server
    private Socket s;
     
    //Socket for incoming chat
    private ServerSocket serverSocket;
    private Socket _client;
     
    //ThreadPool
    private ThreadPoolExecutor executor;
     
    //IO buffer
    BufferedWriter bw;
    BufferedReader br;
    static Process p;
    public static ReliableBroadcast rb;
     
    //constant variable
    private static final int heartbeat_rate = 5;
    private static final String serverAddress = "localhost";
    private static int portNumber = 1222;      //this gets reset to what the user inputs
    private static final int THREAD_POOL_CAPACITY = 10;
     
    public ChatClient() {
        try{
            s = new Socket(serverAddress, portNumber);
            p = new Process();
            rb = new ReliableBroadcast();
            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            serverSocket = new ServerSocket(0);
        }catch (IOException e) {
            System.out.println("Cannection to server failed");
            System.exit(1);
        }
         
        this.executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(THREAD_POOL_CAPACITY);
        this._ip = s.getLocalAddress().toString().substring(1);
        this._port = serverSocket.getLocalPort();
        
        //listen for chat
        this.listen();
    }
     
   
    
 private void listen() {
  this.executor.execute(this);
 }
 
    //register client
    @SuppressWarnings("resource")
 public boolean register() {
        //get the name
        Scanner sc = new Scanner(System.in);
        System.out.print("Please Enter Your Name: ");
        this._name = sc.nextLine();
         
        String m = "register<" + this._name + ", " + this._ip + ", " +  this._port + ">" + "\n";
        //System.out.println(m);
        //System.out.println(this._ip + this._port + this._name);

        //p.Node(this._ip, this._port, this._name); //make the client a process node
        
        this.sendMessage(m);
        //System.out.println("Message sent to the server : " + m);
         
        m = this.readMessage();
        //System.out.println(m);
        //System.out.print(m);
        return(m.equals("Success"));
    }
    
     @SuppressWarnings("resource")
 public boolean autoRegister(String s) {
        //get the name
        //Scanner sc = new Scanner(System.in);
        System.out.print(s);
        this._name = s;
         
        String m = "register<" + this._name + ", " + this._ip + ", " +  this._port + ">" + "\n";
        //System.out.println(m);
         
        this.sendMessage(m);
        //System.out.println("Message sent to the server : " + m);
         
        m = this.readMessage();
        //System.out.println(m);
        //System.out.print(m);
        return(m.equals("Success"));
    }
     
    //send heart beat every heartbeat_rate seconds
    public void sendHeartbeat() {
        this.executor.execute(new Runnable() {

         ChatClient c;
         
   @Override
   public void run() {
    try {
              while(true) {
                  String m = "heartbeat<" + c._name + ", " + c._ip + ", " +  c._port + ">" + "\n";
                  c.sendMessage(m); 
                  //System.out.println("Message sent to the server : " + m);
                  Thread.sleep(heartbeat_rate * 1000);
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
   }
   
   public Runnable init(ChatClient cc) {
    this.c = cc;
    return this;
   }
         
        }.init(this));
    }
     
    //write String s to the Socket
    public boolean sendMessage(String s) {
        try {
            this.bw.write(s);
            this.bw.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
     
    //read message from the socket
    public String readMessage() {
        String s;
        try {
            s = this.br.readLine();
        } catch (IOException e) {
            return null;
        }
        return s;
    }
     
    @Override
    public void run() {
        try {
            this._client = this.serverSocket.accept();
            System.out.print("Incoming chat.\nDo you want to answer?[yn]\n> ");
            return;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return;
    }
     
    private void prompt() {
        Scanner sc = new Scanner(System.in);
        while(true) {
            try {
                //this.listen();
                System.out.print("> ");
                String command = sc.nextLine();
                
                if(command.equals("exit")) {
                    sc.close();
                    System.exit(1);
                } else if(command.equals("get")) {
                    this.group = this.get();
                } 
                else if(command.equals("getp")) {
                    this.getProcess();
                } else if(command.contains("chat")) {
                    this.Chat(command);
                } else if(command.equals("y")) {
                    this.AcceptChat();
                    this.listen();
                } else if(command.equals("n")) {
                    bw = new BufferedWriter(new OutputStreamWriter(this._client.getOutputStream()));
                    bw.write("Declined\n");
                    bw.flush();
                } else {
                  //this.broadCast(command);
                  System.out.println("Command not found"+command);
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
                 
            }
        }
    }
    
    @SuppressWarnings("resource")
    private void broadCast(String s) throws IOException{
      try{
        System.out.println("\t\t\t This is the prcess list size = " + rb.p_group.size());
      }
      catch(NullPointerException e)
      {
        System.out.println("SHIT dint WORK");
      }
    }
    
    //chat from here
    @SuppressWarnings("resource")
 private void Chat(String s) throws IOException {
     String [] tok = s.split(" ");
     Socket socket = new Socket(tok[1], Integer.parseInt(tok[2]));
     BufferedWriter bbw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
     
     //String [] tok = s.split(" ");

     //for(int i = 0; i < rb.p_group.size(); i++)
    // {
       //Process temp = new Process();
       //temp = rb.p_group.get(i);
       //socket = new Socket(temp.getIP(), temp.getPort());
       //bbw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    // }
     //new thread to keep receiving message
     this.executor.execute(new Runnable() {

      Socket s;
      
   @Override
   public void run() {
    try {
     BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
     while(true) {
      String s = br.readLine();
      if(s == null) {
       continue;
      }
      if(s.equals("EndSession")) {
       System.out.println("Chat Closed");
       System.out.print("> ");
       return;
      }
      System.out.println(s);
      System.out.print("> ");
     }
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
   
   private Runnable init(Socket socket) {
    this.s = socket;
    return this;
   }
      
     }.init(socket));
     
     Scanner sc = new Scanner(System.in);
        while(true) {
         System.out.print("> ");
         String st = sc.nextLine();
         if(st.equals("exit")) {
          bbw.write("EndSession\n");
          bbw.flush();
          return;
         }
         //st = st + "\n";
         st = this._name+": "+st + "\n";
         bbw.write(st);
         bbw.flush();
        }
    }
    
    //start chat session
    @SuppressWarnings("resource")
 private void AcceptChat() throws IOException {
     BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(_client.getOutputStream()));
     BufferedReader br = new BufferedReader(new InputStreamReader(_client.getInputStream()));
        
        //printing incoming message
        this.executor.execute(new Runnable() {

         BufferedReader br;
   @Override
   public void run() {
    while(true) {
     try {
      String s = br.readLine();
      if(s == null) {
       continue;
      }
      if(s.equals("EndSession")) {
       System.out.println("Chat Closed");
       System.out.print("> ");
       return;
      }
      System.out.println(s);
      System.out.print("> ");
     } catch(Exception e) {
      e.printStackTrace();
     }
    }
   }
   
   private Runnable init(BufferedReader br) {
    this.br = br;
    return this;
   }
         
        }.init(br));
        Scanner sc = new Scanner(System.in);
        while(true) {
         System.out.print("> ");
         String s = sc.nextLine();
         if(s.equals("exit")) {
          bw.write("EndSession\n");
          bw.flush();
          return;
         }
         //s = s + "\n";
         s = this._name+": "+s+"\n";
         bw.write(s);
         bw.flush();
        }
    }
     
    @SuppressWarnings("unchecked")
    private ArrayList<String> get() {
        ArrayList<String> ret = null;
        try {
            String m = "get\n";
            this.sendMessage(m);
            ObjectInputStream ois = new ObjectInputStream(this.s.getInputStream());
            ret = (ArrayList<String>) ois.readObject();
            System.out.println(ret.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    private ArrayList<Process> getProcess() {
        ArrayList<Process> ret = null;
        try {
            String m = "getp\n";
            this.sendMessage(m);
            ObjectInputStream ois = new ObjectInputStream(this.s.getInputStream());
            ret = (ArrayList<Process>) ois.readObject();
            //System.out.println(ret.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
     
    public static void main(String[] args) throws Exception {
        if(args.length!=1){
     System.out.println("Need port number");
     System.exit(1);
 }
 portNumber=Integer.parseInt(args[0]);

 ChatClient cc = new ChatClient();
        while(true) {
            if(cc.register()) break;
        }
         
        cc.sendHeartbeat();
        cc.prompt();

        
        //ReliableBroadcast rb = new ReliableBroadcast(p); //start from here
        
    }
}
