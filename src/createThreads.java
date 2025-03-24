import java.util.concurrent.Semaphore;

public class createThreads {

    public createThreads(int cantHilos, Runnable proceso, String nameProceso){  //set cant de hilos para habilitar semaforos
                                                                        // y referencia de que clase los genera
        for (int i = 0; i<cantHilos; i++){
            String nameThread = "Thread - "+ i+" - " + nameProceso;
            Thread thread = new Thread(proceso, nameThread);
            thread.start();
        }
    }
}
