import java.util.concurrent.Semaphore;

public class Asiento {
    private String nameAsiento;
    private estadoAsiento statusAsiento;
    private Semaphore semaphoreAsiento; //semaforo para que mas de un hilo modifique el status de un asiento
    private boolean checked;

    public Asiento(String nameAsiento){
        this.nameAsiento = nameAsiento;
        this.statusAsiento = estadoAsiento.LIBRE;
        semaphoreAsiento = new Semaphore(1);
        this.checked = false;
    }

    public String getNameAsiento() { return nameAsiento; }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public boolean getChecked() {
        return checked;
    }
    public void setStatus(estadoAsiento status) {
        try {
            semaphoreAsiento.acquire();
            this.statusAsiento = status;
            semaphoreAsiento.release();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized estadoAsiento getStatus() { return statusAsiento; }  //verificar si realmente hace falta el synchronized
}
