package com.example;

import org.jocl.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.jocl.CL.*;

public class GpuParalela {

    // Kernel OpenCL para contar palavras
    private static final String KERNEL_SOURCE =
            "__kernel void countWords(__global const char* content, int contentLength,\n" +
            "                          __global const char* palavra, int palavraLength,\n" +
            "                          __global int* result) {\n" +
            "    int globalId = get_global_id(0);\n" +
            "    int count = 0;\n" +
            "    for (int i = 0; i <= contentLength - palavraLength; i++) {\n" +
            "        bool match = true;\n" +
            "        for (int j = 0; j < palavraLength; j++) {\n" +
            "            if (palavra[j] != content[i + j]) {\n" +
            "                match = false;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "        if (match) {\n" +
            "            count++;\n" +
            "        }\n" +
            "    }\n" +
            "    result[globalId] = count;\n" +
            "}\n";

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        String filePath = "Dracula.txt"; // Exemplo com Dracula.txt
        System.out.println("Digite a palavra desejada para contar em " + filePath + " :");
        String palavra = s.next();

        long startTime = System.currentTimeMillis();
        int count = parallelGPU(filePath, palavra);
        long endTime = System.currentTimeMillis();

        System.out.println("--- GPU PARALELA ---");
        System.out.println("Palavra: " +palavra);
        System.out.println("Contagem: " + count);
        System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");

        String csvFilePath = "gpu_paralelo_csv.csv";
        String csvContent = "Metodo,Palavra,Aparicoes,Tempo(ms)\n";
        csvContent += "ParaleloGPU," + palavra + "," + count + "," + (endTime - startTime) + "\n";

        Files.write(Paths.get(csvFilePath), csvContent.getBytes());
        System.out.println("CSV: " + csvFilePath);
    }

    public static int parallelGPU(String filePath, String palavra) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        int contentLength = content.length();
        int palavraLength = palavra.length();

        CL.setExceptionsEnabled(true);

        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_GPU, devices.length, devices, null);

        cl_context context = clCreateContext(null, 1, devices, null, null, null);
        @SuppressWarnings("deprecation")
        cl_command_queue commandQueue = clCreateCommandQueue(context, devices[0], 0, null);

        cl_mem contentMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * contentLength, Pointer.to(content.getBytes()), null);
        cl_mem palavraMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * palavraLength, Pointer.to(palavra.getBytes()), null);

        int[] result = new int[1];
        cl_mem resultMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int, Pointer.to(result), null);

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{KERNEL_SOURCE}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "countWords", null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(contentMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{contentLength}));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(palavraMem));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{palavraLength}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resultMem));

        long globalWorkSize = 1;
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, new long[]{globalWorkSize}, null, 0, null, null);

        clEnqueueReadBuffer(commandQueue, resultMem, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(result), 0, null, null);

        clReleaseMemObject(contentMem);
        clReleaseMemObject(palavraMem);
        clReleaseMemObject(resultMem);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        return result[0];
    }
}
