import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Reserva implements Runnable{
    private matrizDeAsientos matriz;
    private final Semaphore semaphore = new Semaphore(1);  //semaforo para que mas de un hilo no modifique el recorrido ni asientosPendientes
    public int recorrido = 0;
    private ArrayList<Asiento> asientosPendientes;
    private ArrayList<Asiento> asientosCancelados;
    private ArrayList<Asiento> asientosConfirmados;
    private ArrayList<Asiento> asientosVerificados;
    private CountDownLatch endResevas;
    private Object lockAsientosPendientes;

    public Reserva(matrizDeAsientos matriz, CountDownLatch endResevas, Object lockAsientosPendientes){
        this.matriz = matriz;
        asientosPendientes = new ArrayList<>();
        asientosCancelados = new ArrayList<>();
        asientosConfirmados = new ArrayList<>();
        asientosVerificados = new ArrayList<>();
        this.endResevas = endResevas;
        this.lockAsientosPendientes = lockAsientosPendientes;
    }

    public String getName(){
        return "Reserva";
    }

    public ArrayList<Asiento> getMatriz() {
        return matriz.getMatriz();
    }
    public synchronized ArrayList<Asiento> getAsientosPendientes() { return asientosPendientes; }
    public synchronized ArrayList<Asiento> getAsientosCancelados() {
        return asientosCancelados;
    }
    public synchronized ArrayList<Asiento> getAsientosConfirmados() { return asientosConfirmados; }
    public synchronized ArrayList<Asiento> getAsientosVerificados() { return asientosVerificados;  }
    private void setAsientoPendiente(Asiento asientosPendientes) {
        // privado porque se usa solo cuando se ejecutan los hilos de esta clase
        this.asientosPendientes.add(asientosPendientes);
    }
    public void setAsientoCancelado(Asiento asientoCancelado) { this.asientosCancelados.add(asientoCancelado); }
    public void setAsientoConfirmado(Asiento asientoConfirmado) { this.asientosConfirmados.add(asientoConfirmado); }
    public void setAsientoVerificado(Asiento asientoVerificado) {
        this.asientosVerificados.add(asientoVerificado);
    }

    @Override
    public void run() {
        while (recorrido < matriz.getCantAsientos()){
            int random = ThreadLocalRandom.current().nextInt(0, matriz.getCantAsientos());
            try {
                TimeUnit.MILLISECONDS.sleep(50);
                semaphore.acquire();
                //seccion critica
                if (matriz.getMatriz().get(random).getStatus() == estadoAsiento.LIBRE) {   //getStatus es synchronized
                    matriz.getMatriz().get(random).setStatus(estadoAsiento.OCUPADO);   //setStatus tiene semaforo
                    setAsientoPendiente(matriz.getMatriz().get(random));  //seteo Asiento pendiente
                    recorrido++;
                    synchronized (lockAsientosPendientes) {
                        lockAsientosPendientes.notify();   //para despertar los hilos de Pago
                    }
                }
                semaphore.release();
                //fin seccion critica
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
//        endResevas.countDown();
    }
}