package server;

import org.w3c.dom.ls.LSOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class Server {
    static ArrayList<Users> users = new ArrayList<>();
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8188);
            System.out.println("Сервер запущен");
            while (true){
                Socket socket = serverSocket.accept();
                Users currentUser = new Users(socket);
                users.add(currentUser);
                System.out.println("Клиент подключился");
                DataInputStream in = new DataInputStream(socket.getInputStream()); //  ввод
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //вывод
                currentUser.setOut(out);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String request = null;
                        try {
                            currentUser.getOut().writeObject("Введите имя: ");
                            String userName = in.readUTF();
                            while(userName.indexOf("/") != -1 || userName.equals("") ||
                                    userName.indexOf(" ") != -1 ){
                                if(userName.indexOf("/") != -1 || userName.indexOf(" ") != -1)
                                    currentUser.getOut().writeObject("Пробелы и символ \"/\" запрещен");
                                else
                                    currentUser.getOut().writeObject("Имя не может быть пустым");
                                currentUser.getOut().writeObject("Введите имя: ");
                                userName = in.readUTF();
                            }
                            currentUser.setUserName(userName);
                            sendUsersSerializable();
                            currentUser.getOut().writeObject("Приветствую, "+currentUser.getUserName()+"!");
                            while (true){
                                request = in.readUTF(); // Принимает сообщение от клиента
                                System.out.println("Клиент "+userName+" прислал: "+request);
                                if(!request.equals("")) {
                                    for (Users user : users) { // Перебираем клиентов которые подключенны в настоящий момент
                                        if (currentUser.getUuid() != user.getUuid()) {
                                            user.getOut().writeObject(currentUser.getUserName() + ": " + request);
                                        } else user.getOut().writeObject("Вы: " + request);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            users.remove(currentUser);
                            for (Users user: users) {
                                try {
                                    user.getOut().writeObject("Пользователь "+currentUser.getUserName()+" покинул чат");
                                    socket.close();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                            sendUsersSerializable();
                        }
                    }
                });
                thread.start();
            }





        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendUsersSerializable(){
        String usersName = "--В сети--";
        for (Users user:users) {
            usersName += "//"+user.getUserName();
        }
        for(Users user : users){
            try {
                user.getOut().writeObject(usersName);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
