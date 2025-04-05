import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Logger implements Runnable {
    private static final String LOG_FILE = "Registro de Reservas.log";
    private Reserva reserva;
    private CountDownLatch endMain;
    private long inicio;
    public Logger(Reserva reserva, CountDownLatch endMain, long inicio){
        this.reserva = reserva;
        this.endMain = endMain;
        this.inicio = inicio;
    }
    public void escribirLog(String mensaje, boolean borrar) {
        try (FileWriter fw = new FileWriter(LOG_FILE, !borrar);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Registrar(){
        escribirLog("----Inicio de Registro de Reservas", true);
        while (true){
            String string = "\n------------------------------------";
            string +="\nAsientos Pendientes: " + reserva.getAsientosPendientes().size();
            string +="\nAsientos Confirmados: " + reserva.getAsientosConfirmados().size();
            string += "\nAsientos Cancelados: " + reserva.getAsientosCancelados().size();
            string += "\nAsientos Verificados: "+ reserva.getAsientosVerificados().size();

            escribirLog(string, false);
            if (endMain.getCount() == 0 ){
                //salgo del while de Logger
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long fin = System.currentTimeMillis();
        escribirLog( "\n--------------Tiempo de ejecución: " + (fin - inicio) + " ms--------------", false);
    }
    public String getName(){ return "Logger"; }
    @Override
    public void run() {
        Registrar();
    }
}
