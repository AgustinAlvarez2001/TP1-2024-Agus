import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Pago implements Runnable{
    private Reserva reserva;
    private Object lockAsientosPendientes;
    private CountDownLatch endPagos;
    private int contador;  ////cantidad de asientos
    private Semaphore semaphore = new Semaphore(1);
    private static final AtomicInteger hilosEnEjecucion = new AtomicInteger(0);

    public Pago(Reserva reserva, Object lockAsientosPendientes, CountDownLatch endPagos){
        //lockAsientosPendientes--> objeto para bloquear/desbloquear
        //endPagos para saber cuando termina el proceso de pago en el main
        this.reserva = reserva;
        this.lockAsientosPendientes = lockAsientosPendientes;
        this.endPagos = endPagos;
        this.contador = reserva.getMatriz().size();
    }
    public void pagar() {
        try {
            //seccion critica
            semaphore.acquire();
            TimeUnit.MILLISECONDS.sleep(130);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        boolean pagoRandom = ThreadLocalRandom.current().nextInt(100) < 90; //genera un true si
        // el numero random entre 0 y 99 es < que 90
        if (pagoRandom) {
            reserva.setAsientoConfirmado(reserva.getAsientosPendientes().remove(0));
            //borro el asiento de asientospendientes
            semaphore.release();
        } else {
            int indiceDelAsiento = reserva.getMatriz().indexOf(reserva.getAsientosPendientes().remove(0));
            //borro el asiento de asientospendientes, y obtengo el indice dentro de la matiz de asientos
            reserva.getMatriz().get(indiceDelAsiento).setStatus(estadoAsiento.DESCARTADO);
            //con el indice seteo el nuevo estado del asiento dentro de la matriz de asientos
            reserva.setAsientoCancelado(reserva.getMatriz().get(indiceDelAsiento));
            //seteo el asiento a la lista de asientos cancelados
            semaphore.release();
        }
    }
    public String getName(){ return "Pago"; }
    @Override
    public void run() {
        hilosEnEjecucion.getAndIncrement();
        while (true){
            synchronized (lockAsientosPendientes){
                while(reserva.getAsientosPendientes().isEmpty()) {
                    try {
                        lockAsientosPendientes.wait();  //cuando reciba el notify significa que va a haber asientos
                                                        //y no va a entrar en la siguiente iteracion del while
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                pagar();
                contador--;
                if (contador <= (hilosEnEjecucion.get()-1)) {
                    //para que salga del while cuando no haya mas asientos pendientes y finalice el run
                    break;
                }
            }
        }
        endPagos.countDown();
    }
}
