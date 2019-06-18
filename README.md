# lan 支持android平台局域网通讯的框架
博客地址：https://blog.csdn.net/dxh040431104/article/details/92789934

使用方法：
服务端：
      
      TcpService tcpService = new TcpService(this);

//注册设备连接的监听
        
        tcpService.setDeviceListener(new IDeviceListener() {
            @Override
            public void onDeviceDisConnect(Device device) {
            }

            @Override
            public void onDeviceConnect(Device device) {

            }
        });
        
//注册消息回调        

    device.setMessageListener(new IMessageListener() {
        @Override
        public void onNewMessage(String data) {
        }
    });

//消息发送

    device.sendMsg("你好")//该方法只是负责消息发送
    device.sendMsgWithAck("你好")//该方法可以知道消息是否发送成功


 客户端：
 
    1：SubTcpClient subTcpClient = new SubTcpClient(this, new IDeviceListener() {
        @Override
        public void onDeviceDisConnect(Device device) {
        }

        @Override
        public void onDeviceConnect(Device device) {
        }
    });
 
 //查找服务器列表
 
    subTcpClient.startScan(new IScanListener() {
            @Override
            public void onScanResult(List<MasterDevice> serviceList) {
                connect(serviceList.get(0));
            }

            @Override
            public void onScanFinish() {
            }
        });   
 
 //连接服务器
 
    subTcpClient.connect(device.getIp());   

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
