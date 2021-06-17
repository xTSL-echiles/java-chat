package server;

import org.w3c.dom.ls.LSOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class Server {
    public static void main(String[] args) {
        ArrayList<Socket> clinetSokets = new ArrayList<>();
        ArrayList<String> clientName = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(8188); // Создаёи серверный сокет
            System.out.println("Сервер запущен");
            while (true) { // бесконечный цикл для ожидания подключения клиентов
                System.out.println("Ожидаю подключения клиентов...");
                Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
                clinetSokets.add(socket);
                System.out.println("Клиент подключился");
                DataInputStream in = new DataInputStream(socket.getInputStream()); // Поток ввода
                DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // Поток вывода
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String request = null;
                        String name = null;
                        try {
                            out.writeUTF("Введите ваше имя в чате");
                            name = in.readUTF(); // Принимает сообщение от клиента
                            clientName.add(name);
                            out.writeUTF("Имя установлено");
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                        while (true){
                            try{
                                request = in.readUTF(); // Принимает сообщение от клиента
                                System.out.println("Клиент прислал: "+request);
                                for (Socket clinetSoket: clinetSokets) { // Перебираем клиентов которые подключенны в настоящий момент
                                    DataOutputStream out = new DataOutputStream(clinetSoket.getOutputStream());
                                    if(!socket.equals(clinetSoket)) {
                                        out.writeUTF(name + ": " + request.toUpperCase(Locale.ROOT)); // Рассылает принятое сообщение всем клиентам
                                    }
                                }
                            }catch (IOException ex){
                                ex.printStackTrace();
                                clinetSokets.remove(socket); // Удаление сокета, когда клиент отключился
                                //String finalName = name;
                                for (int i = 0; i < clientName.toArray().length; i++) {
                                    if(clientName.get(i).equals(name))
                                        clientName.remove(i);
                                }
                                break;
                            }
                        }
                    }
                });
                System.out.println("Пользователи в сети: "+clientName);
                thread.start();
            }





        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

