import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Verificacion implements Runnable{
    private Reserva reserva;
    private CountDownLatch endVerificacion;
    private CountDownLatch endChecked;
    //private Semaphore semaphore = new Semaphore(1);
    private Semaphore semaphore;
//    public Verificacion(Reserva reserva, CountDownLatch endVerificacion, CountDownLatch endChecked){
    public Verificacion(Reserva reserva, CountDownLatch endVerificacion, CountDownLatch endChecked, Semaphore semaphore){
        this.reserva = reserva;
        this.endVerificacion = endVerificacion;
        this.endChecked = endChecked;

        this.semaphore = semaphore;
    }
    public String getName(){
        return "Verificación";
    }
    private void verificar(){
        if(reserva.getAsientosConfirmados().size() == 0){
            System.out.println("No hay para verficar " + Thread.currentThread().getName());
            semaphore.release();
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        int random = ThreadLocalRandom.current().nextInt(0, reserva.getAsientosConfirmados().size());
        if(reserva.getAsientosConfirmados().get(random).getChecked()){
//            Asiento newAsientoVerificado = reserva.getAsientosConfirmados().remove(random);
//            reserva.setAsientoVerificado(newAsientoVerificado);
            reserva.setAsientoVerificado(reserva.getAsientosConfirmados().remove(random));
        }
        semaphore.release();
    }
    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(380);
                semaphore.acquire();
                verificar();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //cuando haya terminado Checked empiezo a revisar si todos los asientos estan Verificados
            if(endChecked.getCount() == 0){
                if(reserva.getAsientosConfirmados().isEmpty()){
                    ///verifica que todos los checked sean iguales (true) para salir del while
                    System.out.println("SALE Verificacion: " + Thread.currentThread().getName());
                    break;
                }
            }
        }
        endVerificacion.countDown();
    }
}
