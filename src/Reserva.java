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

    public synchronized int getRecorrido() { return recorrido; }//funcion solo para ver por Main


    public synchronized ArrayList<Asiento> getAsientosPendientes() { return asientosPendientes; }
    public synchronized ArrayList<Asiento> getAsientosCancelados() {
        return asientosCancelados;
    }
    public synchronized ArrayList<Asiento> getAsientosConfirmados() {
        //lockConfirmados.lock();
        return asientosConfirmados;  ///uso la funcion desde Checked y Verificacion, hay conflicto en .size()
        //lockConfirmados.unlock(); //cuakkkk
    }
    public synchronized ArrayList<Asiento> getAsientosVerificados() { return asientosVerificados;  }
    private void setAsientoPendiente(Asiento asientosPendientes) {
        // privado porque se usa solo cuando se ejecutan los hilos de esta clase
        this.asientosPendientes.add(asientosPendientes);
    }
//    private Lock lockCanceladas = new ReentrantLock(); /// 'das' porque hago referencia a las reservas
    private Lock lockConfirmados= new ReentrantLock();
    public void setAsientoCancelado(Asiento asientoCancelado) {
        //lockCanceladas.lock();                        //para que no se pisen
        this.asientosCancelados.add(asientoCancelado);
        //lockCanceladas.unlock();
    }
    public void setAsientoConfirmado(Asiento asientoConfirmado) {
        //lockVerificadas.lock();
        this.asientosConfirmados.add(asientoConfirmado);
        //lockVerificadas.unlock();
    }
    public void setAsientoVerificado(Asiento asientoVerificado) {
        this.asientosVerificados.add(asientoVerificado);
    }

    @Override
    public void run() {
        while (recorrido < matriz.getCantAsientos()){
            int random = ThreadLocalRandom.current().nextInt(0, matriz.getCantAsientos());
            try {
                //Long duration = (long)(Math.random()*35);
                TimeUnit.MILLISECONDS.sleep(15);
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
        endResevas.countDown();
    }
}
