import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class CpuParalela {

    public static void main(String[] args) throws IOException {

        Scanner s = new Scanner(System.in);
        String filePath = "Dracula.txt"; // Utilizado o Dracula.txt para exemplo
        System.out.println("Digite a palavra desejada para contar em " + filePath + " :");
        String palavra = s.next();

        long startTime = System.currentTimeMillis();
        int count = contarPalavrasParalelo(filePath, palavra);
        long endTime = System.currentTimeMillis();

        System.out.println("--- CPU PARALELA ---");
        System.out.println("Palavra: " + palavra);
        System.out.println("Contagem: " + count);
        System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");

        String csvFilePath = "cpu_paralelo_csv.csv";
        String csvContent = "Metodo,Palavra,Aparicoes,Tempo(ms)\n";
        csvContent += "CpuParalela," + palavra + "," + count + "," + (endTime - startTime) + "\n";

        Files.write(Paths.get(csvFilePath), csvContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        System.out.println("CSV: " + csvFilePath);
    }

    public static int contarPalavrasParalelo(String filePath, String palavra) throws IOException {
        String conteudo = Files.readString(Paths.get(filePath));
        String[] palavras = conteudo.split("\\W+");
        int numProcessadores = Runtime.getRuntime().availableProcessors(); //Usar todos os processadores disponiveis

        ForkJoinPool pool = new ForkJoinPool(numProcessadores);
        ContadorDePalavras tarefa = new ContadorDePalavras(palavras, palavra, 0, palavras.length);
        return pool.invoke(tarefa);
    }

    static class ContadorDePalavras extends RecursiveTask<Integer> {
        private static final int LIMITE = 1000;
        private String[] palavras;
        private String palavra;
        private int inicio;
        private int fim;

        ContadorDePalavras(String[] palavras, String palavra, int inicio, int fim) {
            this.palavras = palavras;
            this.palavra = palavra;
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        protected Integer compute() {
            if (fim - inicio <= LIMITE) {
                return (int) Arrays.stream(palavras, inicio, fim)
                                   .filter(word -> word.equalsIgnoreCase(palavra))
                                   .count();
            } else {
                int meio = (inicio + fim) / 2;
                ContadorDePalavras tarefa1 = new ContadorDePalavras(palavras, palavra, inicio, meio);
                ContadorDePalavras tarefa2 = new ContadorDePalavras(palavras, palavra, meio, fim);
                invokeAll(tarefa1, tarefa2);
                return tarefa1.join() + tarefa2.join();
            }
        }
    }
}
