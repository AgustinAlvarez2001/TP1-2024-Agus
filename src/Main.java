import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        long inicio = System.currentTimeMillis();  //inicio del programa

        CountDownLatch endReservas = new CountDownLatch(3); // No hace falta
        CountDownLatch endPagos = new CountDownLatch(2);
        CountDownLatch endChecked = new CountDownLatch(3);
        CountDownLatch endVerificacion = new CountDownLatch(1); ///uno porque el otro hilo va quedar dormido en la ultima iteracion, y cuando se despierta sale
        CountDownLatch endMain = new CountDownLatch(1);
        Semaphore semaphore = new Semaphore(1); ///para checked y verificacion
        Object lockAsientoPendientes = new Object(); //para Reserva y Pago

        int cantAsientos = 186;
        matrizDeAsientos matriz = new matrizDeAsientos(cantAsientos); //Matriz de Asientos

        Reserva reserva = new Reserva(matriz, endReservas, lockAsientoPendientes);
        createThreads reservas = new createThreads(3, reserva, reserva.getName()); //Hilos para reservar Asientos en la Matriz

        Logger logger = new Logger(reserva,endMain,inicio);
        createThreads hiloLogger = new createThreads(1,logger,logger.getName());

//        endReservas.await();
        Pago pago = new Pago(reserva, lockAsientoPendientes, endPagos); //Hilos para confirmar/cancelar reservas
        createThreads pagos = new createThreads(2, pago, pago.getName());
//        endPagos.await();
        Checked checked = new Checked(reserva, endChecked, endPagos, semaphore);
        createThreads checkeds = new createThreads(3, checked, checked.getName()); //Hilos para hacer el checked
//        endChecked.await();
        Verificacion verificacion = new Verificacion(reserva, endVerificacion, endChecked, semaphore);
        createThreads verificaciones = new createThreads(2,verificacion, verificacion.getName()); //Hilos para hacer las verificaciones

        endVerificacion.await();
        endMain.countDown(); //para salir del while del Logger
    }
}