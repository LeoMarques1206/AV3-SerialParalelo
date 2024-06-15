import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;

public class CpuSerial {

    public static void main(String[] args) throws IOException {

        Scanner s = new Scanner(System.in);
        String filePath = "Dracula.txt"; //Utilizado o Dracula.txt para exemplo
        System.out.println("Digite a palavra desejada para contar em " +filePath+ " :");
        String palavra = s.next();

        long startTime = System.currentTimeMillis();
        int count = contarPalavras(filePath, palavra);
        long endTime = System.currentTimeMillis();

        System.out.println("--- CPU SERIAL ---");
        System.out.println("Palavra: " + palavra);
        System.out.println("Contagem: " + count);
        System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");

        String csvFilePath = "cpu_serial_csv.csv";
        String csvContent = "Metodo,Palavra,Aparicoes,Tempo(ms)\n";
        csvContent += "CpuSerial," + palavra + "," + count + "," + (endTime - startTime) + "\n";

        Files.write(Paths.get(csvFilePath), csvContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        System.out.println("CSV: " + csvFilePath);
         
    }

    public static int contarPalavras(String filePath, String palavra) throws IOException {
        String conteudo = Files.readString(Paths.get(filePath));
        String[] palavras = conteudo.split("\\W+");
        return (int) Arrays.stream(palavras)
                           .filter(word -> word.equalsIgnoreCase(palavra))
                           .count();
    }
}
