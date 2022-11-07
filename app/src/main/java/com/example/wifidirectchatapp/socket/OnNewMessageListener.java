package com.example.wifidirectchatapp.socket;

import com.example.wifidirectchatapp.my_socket_library.IO;
import com.example.wifidirectchatapp.my_socket_library.SingleSocket;
import com.example.wifidirectchatapp.my_socket_library.model.MessagePacket;

public class OnNewMessageListener implements IO.OnNewMessageListener {
    private static final String TAG = "New Message";

    @Override
    public void onNewMessage(SingleSocket socket, MessagePacket MessagePacket) {

        Server.getInstance().emitAllExcept((MySocket) socket,MessagePacket);

        if (MessagePacket.isFile()) {
            saveFileFromStream(MessagePacket);
        } else {
            if (MessagePacket.getEvent().equalsIgnoreCase(IO.SEND_MESSAGE)) {
                Message message = new Message(MessagePacket.getSender(), MessagePacket.getMessage(), false, false);
                MessageManager.getInstance().addMessage(message);
            } else {
                socket.disconnectListener.onDisconnect(socket);
            }
        }
    }

    private void saveFileFromStream(MessagePacket message) {

        String filename = message.getMessage();

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        Log.d(TAG, "File name: " + message.getMessage());
        Log.d(TAG, "File path: " + dir.getPath());

        if(dir.exists()) {
            dir.mkdirs();
        }

        try {
            File file = new File(dir.getPath(),filename);


            byte[] bytes = message.getData();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.close();
            Message msg = new Message(message.getSender(), file.getPath(), false, true);
            MessageManager.getInstance().addMessage(msg);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

