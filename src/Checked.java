import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Checked implements Runnable {
    private Reserva reserva;
    private CountDownLatch endChecked;
    private CountDownLatch endPagos;
    private Semaphore semaphore;
    public Checked(Reserva reserva, CountDownLatch endChecked, CountDownLatch endPagos, Semaphore semaphore ){
        this.reserva = reserva;
        this.endChecked = endChecked;
        this.endPagos = endPagos;
        this.semaphore = semaphore;
    }
    public String getName(){
        return "Checked";
    }
    private void checkear(){
        if(reserva.getAsientosConfirmados().size() == 0){
            semaphore.release();
            return;
        }
        int random = ThreadLocalRandom.current().nextInt(0, reserva.getAsientosConfirmados().size());
        boolean trueChecked = ThreadLocalRandom.current().nextInt(100) < 90; //genera un true si

        //verifico el checked si es falso, si es falso -> se valida o cancela
        //si se valida pasa a true, si se cancela se saca el asiento de la lista y se pasa a estado Descartado
        if(!reserva.getAsientosConfirmados().get(random).getChecked()) {
            if (trueChecked){
                reserva.getAsientosConfirmados().get(random).setChecked(true);
            }
            else {
                Asiento newAsientoCancelado = reserva.getAsientosConfirmados().remove(random);  //saco el asiento de verificados
                newAsientoCancelado.setStatus(estadoAsiento.DESCARTADO); //seteo el nuevo status
                reserva.setAsientoCancelado(newAsientoCancelado); //agrego el asiento a AsientoCancelado
            }
        }
        semaphore.release();
    }
    public void run(){
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(130);
                semaphore.acquire();
                checkear();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //cuando haya terminado Pago empiezo a revisar si todos los asientos estan checkeados
            if(endPagos.getCount() == 0){
                if(reserva.getAsientosConfirmados().stream().allMatch(Asiento::getChecked)){
                    ///verifica que todos los checked sean iguales (true) para salir del while
                    break;
                }
            }
        }
        endChecked.countDown();
    }
}