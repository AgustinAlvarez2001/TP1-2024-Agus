import java.util.ArrayList;

public class matrizDeAsientos {
    private final int cantAsientos;
    private final ArrayList<Asiento> matriz;
    public matrizDeAsientos(int cantAsientos){
        this.cantAsientos = cantAsientos;
        this.matriz = generarMatriz();
    }
    private ArrayList<Asiento> generarMatriz(){
        ArrayList<Asiento> matriz = new ArrayList<>(cantAsientos);
        for (int i=0; i<cantAsientos; i++){
            Asiento asiento = new Asiento("Asiento numero "+i);
            matriz.add(i,asiento);
        }
        return matriz;
    }
    public ArrayList<Asiento> getMatriz() {
        return matriz;
    }
    public int getCantAsientos() { return cantAsientos; }

    @Override
    public String toString() {
        String result[] = new String[cantAsientos];
        String show = "";
        for (int i=0; i<cantAsientos; i++){
            if(matriz.get(i).getStatus() == estadoAsiento.LIBRE){
                result[i] = "Lib";
            } else if(matriz.get(i).getStatus() == estadoAsiento.OCUPADO){
                result[i] = " O  ";
            } else {
                result[i] = "DES ";
            }
            if((i%12) == 0) {
                show += "\n";
            }else if ((i%6) == 0) {
                show += "   ";
            }
            show += result[i];
        }
        return show;
    }
}