
package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;

/**
 *
 * @author netosolis
 */
public class HiloServidor implements Runnable{
    //Declaramos las variables que utiliza el hilo para estar recibiendo y mandando mensajes
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    //Varible para guardar que le toco al jugador X o O
    private int XO;
    //Matriz del juego
    private int G[][];
    //Turno
    private boolean turno;
    //Lista de los usuarios conectados al servidor
    private LinkedList<Socket> usuarios = new LinkedList<Socket>();
    
    //Constructor que recibe el socket que atendera el hilo y la lista de los jugadores el turno y la matriz del juego
    public HiloServidor(Socket soc,LinkedList users,int xo,int[][] Gato){
        socket = soc;
        usuarios = users;
        XO = xo;
        G = Gato;
    }
    
    
    @Override
    public void run() {
        try {
            //Inicializamos los canales de comunicacion y mandamos el turno a cada jugador
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            turno = XO == 1;
            String msg = "";
            msg += "JUEGAS: " + (turno ? "X;":"O;");
            msg += turno;
            out.writeUTF(msg);
            
            
            //Ciclo infinito que estara escuchando por los movimientos de cada jugador
            //Cada que un jugador pone una X o O viene aca y le dice al otro jugador que es su turno
            while(true){
                //Leer los datos que se mandan cuando se selecciona un boton
                String recibidos = in.readUTF();
                String recibido [] = recibidos.split(";");
                
                /*
                    recibido[0] : fila del tablero
                    recibido[1] : columna del tablero
                
                */
                
                int f = Integer.parseInt(recibido[0]);
                int c = Integer.parseInt(recibido[1]);
                /*
                    Se guarda la jugada en la matriz
                    X : 1
                    O : 0
                
                */
                G[f][c] = XO;
                
                /*
                Se forma una cadena que se enviara a los jugadores, que lleva informacion del movimiento que se 
                acaba de hacer
                */
                String cad = "";
                cad += XO+";";
                cad += f+";";
                cad += c+";";
                
                /*
                Se comprueba si alguien de los jugadores gano
                y si el tablero ya se lleno... En los dos casos se notifica a los jugadores para empezar de nuevo la partida
                */
                boolean ganador = gano(XO);
                boolean completo = lleno();
                
                if(!ganador && !completo){
                    cad += "NADIE";
                }
                else if(!ganador && completo){
                    cad += "EMPATE";
                }
                else if(ganador){
                    vaciarMatriz();
                    cad += XO == 1 ? "X":"O";
                }
                
                
                
                for (Socket usuario : usuarios) {
                    out = new DataOutputStream(usuario.getOutputStream());
                    out.writeUTF(cad);
                }
            }
        } catch (Exception e) {
            
            //Si ocurre un excepcion lo mas seguro es que sea por que algun jugador se desconecto asi que lo quitamos de la lista de conectados
            for (int i = 0; i < usuarios.size(); i++) {
                if(usuarios.get(i) == socket){
                    usuarios.remove(i);
                    break;
                } 
            }
            vaciarMatriz();
        }
    }
    
    //Funcion comprueba si algun jugador ha ganado el juego
    public boolean gano(int n){
        for (int i = 0; i < 3; i++) {
            boolean gano = true;
            for (int j = 0; j < 3; j++) {
                 gano = gano && (G[i][j] == n); 
            }
            if(gano){
                return true;
            }
        }
        
        for (int i = 0; i < 3; i++) {
            boolean gano = true;
            for (int j = 0; j < 3; j++) {
                 gano = gano && (G[j][i] == n); 
            }
            if(gano){
                return true;
            }
        }
        
        if(G[0][0] == n && G[1][1] == n && G[2][2] == n)return true;
        
        return false;
    }
    
    //Funcion comprueba si el tablero ya esta lleno
    public boolean lleno(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(G[i][j] == -1)return false;
            }
        }
        
        vaciarMatriz();
        return true;
    }
    
    //Funcion para reiniciar la matriz del juego
    public void vaciarMatriz(){
        for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    G[i][j] = -1;
                }
        }
    }
}
