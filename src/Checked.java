import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Checked implements Runnable {
    private Reserva reserva;
    private CountDownLatch endChecked;
    private CountDownLatch endPagos;
    private Object lockAsientoPago;
    //private Semaphore semaphore = new Semaphore(1);
    private Semaphore semaphore;
//    public Checked(Reserva reserva, CountDownLatch endChecked, Object lockAsientoPago, CountDownLatch endPagos ){
    public Checked(Reserva reserva, CountDownLatch endChecked, Object lockAsientoPago, CountDownLatch endPagos, Semaphore semaphore ){
        this.reserva = reserva;
        this.endChecked = endChecked;
        this.lockAsientoPago = lockAsientoPago;
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
                TimeUnit.MILLISECONDS.sleep(150);
                semaphore.acquire();
                checkear();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //cuando haya terminado Pago empiezo a revisar si todos los asientos estan checkeados
            if(endPagos.getCount() == 0){
                if(reserva.getAsientosConfirmados().stream().allMatch(Asiento::getChecked)){
                    ///verifica que todos los checked sean iguales (true) para salir del while
                    System.out.println("SALE Checked: " + Thread.currentThread().getName());
                    break;
                }
            }
        }
        endChecked.countDown();
    }
}



//    public void run(){
//        while (true) { // mientra asientos verificados no este vacio
//            synchronized (lockAsientoPago) {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(30);
//                    if (endPagos.getCount() != 0){   //si o si el proceso de Pago va a terminar antes que el de Checked
//                        System.out.println("Entra Checked: "+ Thread.currentThread().getName());
////                        semaphore.acquire();
////                        checkear();
//                        lockAsientoPago.wait();   //para poner en espera los hilos de Checked
//                        semaphore.acquire();
//                        checkear();
//                    }
//                    else {
//                        lockAsientoPago.notifyAll();        //despierta todos los hilos post endPagos
//                        if(reserva.getAsientosVerificados().stream().allMatch(Asiento::getChecked)){
//                        ///verifica que todos los checked sean iguales (true) para salir del while
//                            System.out.println("SALE Checked: " + Thread.currentThread().getName());
//                            break;
//                        }
//                        semaphore.acquire();
//                        checkear();
//                    }
////                    semaphore.acquire();
////                    checkear();
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        endChecked.countDown();
//    }
//}
