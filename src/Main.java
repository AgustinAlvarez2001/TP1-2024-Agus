import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch endReservas = new CountDownLatch(3);
        CountDownLatch endPagos = new CountDownLatch(2);
        CountDownLatch endChecked = new CountDownLatch(3);
        CountDownLatch endVerificacion = new CountDownLatch(1); ///uno porque el otro hilo va quedar dormido en la ultima iteracion, y cuando se despierta sale
        CountDownLatch endMain = new CountDownLatch(1);

        long inicio = System.currentTimeMillis();

        Object lockAsientoPendientes = new Object();
        Object lockAsientoPago = new Object();    ///no se usa
        //Matriz de Asientos
        int cantAsientos = 186;
        matrizDeAsientos matriz = new matrizDeAsientos(cantAsientos);

        //Thread.sleep(5000);
        //Hilos para reservar Asientos en la Matriz
        Reserva reserva = new Reserva(matriz, endReservas, lockAsientoPendientes);
        createThreads reservas = new createThreads(3, reserva, reserva.getName());
        //Hilos para confirmar/cancelar reservas

        //Genero un hilo logger porque se enojan los otros si uso el hilo Main
        Logger logger = new Logger(reserva,endMain,inicio);
        createThreads hiloLogger = new createThreads(1,logger,logger.getName());

        //Pago pago = new Pago(matriz, reserva, lockAsientoPendientes, endPagos);
        Pago pago = new Pago(reserva, lockAsientoPendientes, lockAsientoPago, endPagos); //lockAsientoPago
        createThreads pagos = new createThreads(2, pago, pago.getName());

        Semaphore semaphore = new Semaphore(1); ///para checked y verificacion

        Checked checked = new Checked(reserva, endChecked, lockAsientoPago, endPagos, semaphore);
        createThreads checkeds = new createThreads(3, checked, checked.getName());

        Verificacion verificacion = new Verificacion(reserva, endVerificacion, endChecked, semaphore);
        createThreads verificaciones = new createThreads(2,verificacion, verificacion.getName());

        endReservas.await();
        System.out.println("------------------Termino Reserva------------");
        endPagos.await();
        System.out.println("------------------Termino Pago------------");
        endChecked.await();
        System.out.println("------------------Termino Checked------------");
        endVerificacion.await();
        System.out.println("------------------Termino Verificacion------------");
        endMain.countDown();

////////////////// ver si los hilos estan vivos simultaneamente ///////////////////
        long fin = System.currentTimeMillis();
        System.out.println("-----------------Tiempo de ejecución: " + (fin - inicio) + " ms------------------");
        System.out.println("Asientos Pendientes: " + reserva.getAsientosPendientes().size());
        System.out.println("Asientos Confirmados: " + reserva.getAsientosConfirmados().size());
        System.out.println("Asientos Cancelados: " + reserva.getAsientosCancelados().size());
        System.out.println("Asientos Verificados: " + reserva.getAsientosVerificados().size());
        System.out.println(matriz);

//        for (int i=0; i<reserva.getAsientosConfirmados().size(); i+=13){
//            System.out.println(reserva.getAsientosConfirmados().get(i).getChecked());
//        }
//        for (int i=0; i<reserva.getAsientosCancelados().size(); i+=2){
//            System.out.println(reserva.getAsientosCancelados().get(i).getChecked());
//        }


    }
}
//        Thread.sleep(5000);
//        System.out.println(reserva.getAsientosPendientes().size());
//        System.out.println(reserva.getRecorrido());
//        for (int i=0; i< matrix.getCantAsientos(); i++){
//            String nameAsientos = reserva.getAsientosPendientes().get(i).getNameAsiento();
//            System.out.println(nameAsientos);
//        }
//        System.out.println(matrix.toString());
//        System.out.println(reserva.getRecorrido());
//        Thread.sleep(1000);
//        System.out.println(matrix.toString());
//        System.out.println(reserva.getRecorrido());
//        Thread.sleep(2000);
//        System.out.println(matrix.toString());
//        System.out.println(reserva.getRecorrido());
//

//        System.out.println(reserva.getAsientosPendientes().size());
//        System.out.println(reserva.getRecorrido());