package com.simulador;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Simulador {

    static class Cliente {
        double tempoChegada;
        double tempoAtendimento;

        Cliente(double tempoChegada, double tempoAtendimento) {
            this.tempoChegada = tempoChegada;
            this.tempoAtendimento = tempoAtendimento;
        }
    }

    static class Fila {
        String nome;
        int capacidade;
        int servidores;
        double atendimentoMinimo;
        double atendimentoMaximo;
        Queue<Cliente> clientes;
        Random random;

        Fila(String nome, int servidores, int capacidade, double atendimentoMinimo, double atendimentoMaximo) {
            this.nome = nome;
            this.servidores = servidores;
            this.capacidade = capacidade;
            this.atendimentoMinimo = atendimentoMinimo;
            this.atendimentoMaximo = atendimentoMaximo;
            this.clientes = new LinkedBlockingQueue<>();
            this.random = new Random();
        }

        double gerarTempoAtendimento() {
            return atendimentoMinimo + (atendimentoMaximo - atendimentoMinimo) * random.nextDouble();
        }

        boolean adicionarCliente(Cliente cliente) {
            if (clientes.size() < capacidade) {
                clientes.add(cliente);
                return true;
            }
            return false;
        }

        Cliente atenderCliente() {
            return clientes.poll();
        }

        int tamanho() {
            return clientes.size();
        }
    }

    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        InputStream inputStream = Simulador.class.getClassLoader().getResourceAsStream("config.yml");
        Map<String, Object> config = yaml.load(inputStream);

        Fila fila1 = configurarFila(config, "fila1");
        Fila fila2 = configurarFila(config, "fila2");
        Fila fila3 = configurarFila(config, "fila3");

        double probFila1ParaFila2 = obterProbabilidade(config, "prob_fila1_para_fila2");
        double probFila1ParaFila3 = obterProbabilidade(config, "prob_fila1_para_fila3");
        double probFila2ParaFila2 = obterProbabilidade(config, "prob_fila2_para_fila2");
        double probFila2ParaFila1 = obterProbabilidade(config, "prob_fila2_para_fila1");
        double probFila2Saida = obterProbabilidade(config, "prob_fila2_saida");
        double probFila3ParaFila3 = obterProbabilidade(config, "prob_fila3_para_fila3");
        double probFila3Saida = obterProbabilidade(config, "prob_fila3_saida");

        Random random = new Random();
        int totalClientesAtendidos = 0;

        int[] atendidosFila1 = new int[5];
        int[] atendidosFila2 = new int[5];
        int[] atendidosFila3 = new int[11];

        int totalEntradasFila1 = 0;
        int[] entradasFila1PorMinuto = new int[5]; // Entradas por minuto (2, 3, 4)
        int totalFila1ParaFila2 = 0;
        int totalFila1ParaFila3 = 0;

        int totalFila2ParaFila2 = 0;
        int totalFila2ParaFila1 = 0;
        int totalFila2Saida = 0;

        int totalFila3ParaFila3 = 0;
        int totalFila3Saida = 0;

        // Inicializar cliente inicial
        Cliente clienteInicial = new Cliente(2, fila1.gerarTempoAtendimento());
        fila1.adicionarCliente(clienteInicial);
        totalEntradasFila1++;
        entradasFila1PorMinuto[0]++; // Contabiliza a entrada no minuto 2

        for (int minuto = 0; minuto < 15; minuto++) {
            for (int i = 0; i < fila1.servidores; i++) { // Limita a quantidade de atendimentos pelo número de servidores
                // Atender fila 1
                Cliente cliente = fila1.atenderCliente();
                if (cliente != null) {
                    totalClientesAtendidos++;
                    if (minuto < 5) {
                        atendidosFila1[minuto]++;
                    }
                    double prob = random.nextDouble();
                    if (prob < probFila1ParaFila2) {
                        cliente.tempoAtendimento = fila2.gerarTempoAtendimento();
                        fila2.adicionarCliente(cliente);
                        totalFila1ParaFila2++;
                    } else {
                        cliente.tempoAtendimento = fila3.gerarTempoAtendimento();
                        fila3.adicionarCliente(cliente);
                        totalFila1ParaFila3++;
                    }
                }
            }

            // Atender fila 2
            for (int i = 0; i < fila2.servidores; i++) {
                Cliente cliente = fila2.atenderCliente();
                if (cliente != null) {
                    totalClientesAtendidos++;
                    if (minuto >= 4 && minuto < 9) atendidosFila2[minuto - 4]++;
                    double prob = random.nextDouble();
                    if (prob < probFila2ParaFila2) {
                        cliente.tempoAtendimento = fila2.gerarTempoAtendimento();
                        fila2.adicionarCliente(cliente);
                        totalFila2ParaFila2++;
                    } else if (prob < probFila2ParaFila2 + probFila2ParaFila1) {
                        cliente.tempoAtendimento = fila1.gerarTempoAtendimento();
                        fila1.adicionarCliente(cliente);
                        totalFila2ParaFila1++;
                    } else {
                        totalFila2Saida++;
                    }
                }
            }

            // Atender fila 3
            // Atender fila 3
for (int i = 0; i < fila3.servidores; i++) {
    Cliente cliente = fila3.atenderCliente();
    if (cliente != null) {
        totalClientesAtendidos++;
        if (minuto >= 5) atendidosFila3[minuto - 5]++;
        double prob = random.nextDouble();
        if (prob < probFila3ParaFila3) {
            cliente.tempoAtendimento = fila3.gerarTempoAtendimento();
            fila3.adicionarCliente(cliente);
            totalFila3ParaFila3++;
        } else {
            totalFila3Saida++;
        }
    }
}

// Ajustar a distribuição percentual de atendimentos da fila 3
if (minuto >= 5) {
    // Armazenar atendimentos e saídas da fila 3
    int totalAtendidosFila3 = atendidosFila3[minuto - 5];
    
    // Obter total de atendimentos e saídas entre minutos 5 e 15
    int totalAtendidosEsaidas = totalFila3ParaFila3 + totalFila3Saida;

    // Se houver atendimentos ou saídas
    if (totalAtendidosEsaidas > 0) {
        // Calcular o percentual de atendimentos
        double percentualAtendidos = (totalAtendidosFila3 / (double) totalAtendidosEsaidas) * 100;
        
        // Armazenar o percentual em um array para ajuste
        double[] distribuicaoFila3 = new double[11];
        
        // Distribuição proporcional
        for (int i = 0; i < 11; i++) {
            distribuicaoFila3[i] = (atendidosFila3[i] / (double) totalAtendidosEsaidas) * 100;
        }

        // Ajustar a distribuição para somar 100%
        double somaDistribuicao = Arrays.stream(distribuicaoFila3).sum();
        for (int i = 0; i < 11; i++) {
            atendidosFila3[i] = (int) Math.round((distribuicaoFila3[i] / somaDistribuicao) * 100);
        }
    }
}

            
            // Ajustar o percentual de transferências da fila 3
            if (minuto >= 5) {
                int totalAtendidosFila3 = atendidosFila3[minuto - 5];
                int totalSaidaFila3 = totalFila3Saida;
            
                // Se houver atendimentos ou saídas
                if (totalAtendidosFila3 + totalSaidaFila3 > 0) {
                    // Calcular o percentual de atendimentos e saídas
                    double percentualAtendidos = (totalAtendidosFila3 / (double) (totalAtendidosFila3 + totalSaidaFila3)) * 100;
                    double percentualSaida = (totalSaidaFila3 / (double) (totalAtendidosFila3 + totalSaidaFila3)) * 100;
            
                    // Atribuir os valores arredondados
                    atendidosFila3[minuto - 5] = (int) Math.round(percentualAtendidos);
                    totalFila3Saida = (int) Math.round(percentualSaida);
                }
            }

            // Registra novos clientes na fila 1 em cada minuto
            if (minuto >= 2 && minuto < 5) {
                Cliente novoCliente = new Cliente(minuto, fila1.gerarTempoAtendimento());
                fila1.adicionarCliente(novoCliente);
                totalEntradasFila1++;
                entradasFila1PorMinuto[minuto - 2]++; // Contabiliza a entrada no minuto correspondente
            }
        }

        // Ajustar o percentual atendido nos minutos 4 a 8 da fila 2 para somar 100%
        int totalAtendidosFila2 = atendidosFila2[0] + atendidosFila2[1] + atendidosFila2[2] + atendidosFila2[3] + atendidosFila2[4];
        if (totalAtendidosFila2 > 0) {
            for (int i = 0; i < 5; i++) {
                atendidosFila2[i] = (int) Math.round((atendidosFila2[i] / (double) totalAtendidosFila2) * 100);
            }
        }

        // Ajustar o percentual de transferências da fila 2
        int totalTransferenciasFila2 = totalFila2ParaFila2 + totalFila2ParaFila1 + totalFila2Saida;
        if (totalTransferenciasFila2 > 0) {
            totalFila2ParaFila2 = (int) Math.round((totalFila2ParaFila2 / (double) totalTransferenciasFila2) * 100);
            totalFila2ParaFila1 = (int) Math.round((totalFila2ParaFila1 / (double) totalTransferenciasFila2) * 100);
            totalFila2Saida = (int) Math.round((totalFila2Saida / (double) totalTransferenciasFila2) * 100);
        }

        // Exibir resultados formatados
        System.out.printf("%% de pessoas que entraram no minuto 2: %.2f%%%n", calcularPercentual(entradasFila1PorMinuto[0], totalEntradasFila1));
        System.out.printf("%% de pessoas que entraram no minuto 3: %.2f%%%n", calcularPercentual(entradasFila1PorMinuto[1], totalEntradasFila1));
        System.out.printf("%% de pessoas que entraram no minuto 4: %.2f%%%n", calcularPercentual(entradasFila1PorMinuto[2], totalEntradasFila1));

        // Calcular total de atendidos no minuto 1 e 2
int totalAtendidosMinuto1 = atendidosFila1[0];
int totalAtendidosMinuto2 = atendidosFila1[1];

// Total de atendidos para minutos 1 e 2
int totalAtendidos = totalAtendidosMinuto1 + totalAtendidosMinuto2;

// Ajustar percentuais para somar 100%
if (totalAtendidos > 0) {
    double percentualMinuto1 = (totalAtendidosMinuto1 / (double) totalAtendidos) * 100;
    double percentualMinuto2 = (totalAtendidosMinuto2 / (double) totalAtendidos) * 100;
    
    System.out.printf("%% de pessoas atendidas no minuto 1 na FILA 1: %.2f%%%n", percentualMinuto1);
    System.out.printf("%% de pessoas atendidas no minuto 2 na FILA 1: %.2f%%%n", percentualMinuto2);
} else {
    System.out.printf("%% de pessoas atendidas no minuto 1 na FILA 1: 0%%%n");
    System.out.printf("%% de pessoas atendidas no minuto 2 na FILA 1: 0%%%n");
}


        // Cálculo de percentuais para transferências da fila 1
        int totalTransferenciasFila1 = totalFila1ParaFila2 + totalFila1ParaFila3;
        double percentualFila1ParaFila2 = totalTransferenciasFila1 > 0 ? (totalFila1ParaFila2 / (double) totalTransferenciasFila1) * 100 : 0;
        double percentualFila1ParaFila3 = totalTransferenciasFila1 > 0 ? (totalFila1ParaFila3 / (double) totalTransferenciasFila1) * 100 : 0;

        System.out.printf("%% de pessoas que foram da FILA 1 para FILA 2: %.2f%%%n", percentualFila1ParaFila2);
        System.out.printf("%% de pessoas que foram da FILA 1 para FILA 3: %.2f%%%n", percentualFila1ParaFila3);
int totalAtendidosFila3 = 0;
for (int i = 0; i < 11; i++) {
    totalAtendidosFila3 += atendidosFila3[i];
}

if (totalAtendidosFila3 > 0) {
    for (int i = 0; i < 11; i++) {
        atendidosFila3[i] = (int) Math.round((atendidosFila3[i] / (double) totalAtendidosFila3) * 100);
    }
}
        // Exibir resultados da fila 2
        System.out.printf("%% de pessoas atendidas no minuto 4 na FILA 2: %d%%%n", atendidosFila2[0]);
        System.out.printf("%% de pessoas atendidas no minuto 5 na FILA 2: %d%%%n", atendidosFila2[1]);
        System.out.printf("%% de pessoas atendidas no minuto 6 na FILA 2: %d%%%n", atendidosFila2[2]);
        System.out.printf("%% de pessoas atendidas no minuto 7 na FILA 2: %d%%%n", atendidosFila2[3]);
        System.out.printf("%% de pessoas atendidas no minuto 8 na FILA 2: %d%%%n", atendidosFila2[4]);

        System.out.printf("%% de pessoas que foram da FILA 2 para FILA 2: %d%%%n", totalFila2ParaFila2);
        System.out.printf("%% de pessoas que foram da FILA 2 para FILA 1: %d%%%n", totalFila2ParaFila1);
        System.out.printf("%% de pessoas que foram da FILA 2 para FORA: %d%%%n", totalFila2Saida);

        // Exibir resultados da fila 3
        System.out.printf("%% de pessoas atendidas no minuto 5 na FILA 3: %d%n", atendidosFila3[0]);
        System.out.printf("%% de pessoas atendidas no minuto 6 na FILA 3: %d%n", atendidosFila3[1]);
        System.out.printf("%% de pessoas atendidas no minuto 7 na FILA 3: %d%n", atendidosFila3[2]);
        System.out.printf("%% de pessoas atendidas no minuto 8 na FILA 3: %d%n", atendidosFila3[3]);
        System.out.printf("%% de pessoas atendidas no minuto 9 na FILA 3: %d%n", atendidosFila3[4]);
        System.out.printf("%% de pessoas atendidas no minuto 10 na FILA 3: %d%n", atendidosFila3[5]);
        System.out.printf("%% de pessoas atendidas no minuto 11 na FILA 3: %d%n", atendidosFila3[6]);
        System.out.printf("%% de pessoas atendidas no minuto 12 na FILA 3: %d%n", atendidosFila3[7]);
        System.out.printf("%% de pessoas atendidas no minuto 13 na FILA 3: %d%n", atendidosFila3[8]);
        System.out.printf("%% de pessoas atendidas no minuto 14 na FILA 3: %d%n", atendidosFila3[9]);
        System.out.printf("%% de pessoas atendidas no minuto 15 na FILA 3: %d%n", atendidosFila3[10]);

        // Calcular total de transferências da FILA 3
int totalTransferenciasFila3 = totalFila3ParaFila3 + totalFila3Saida;

// Ajustar percentuais para somar 100%
if (totalTransferenciasFila3 > 0) {
    double percentualFila3ParaFila3 = (totalFila3ParaFila3 / (double) totalTransferenciasFila3) * 100;
    double percentualFila3Saida = (totalFila3Saida / (double) totalTransferenciasFila3) * 100;

    System.out.printf("%% de pessoas que foram da FILA 3 para FILA 3: %.2f%%%n", percentualFila3ParaFila3);
    System.out.printf("%% de pessoas que foram da FILA 3 para FORA: %.2f%%%n", percentualFila3Saida);
} else {
    System.out.printf("%% de pessoas que foram da FILA 3 para FILA 3: 0%%%n");
    System.out.printf("%% de pessoas que foram da FILA 3 para FORA: 0%%%n");
}

    }

    private static double obterProbabilidade(Map<String, Object> config, String chave) {
        return ((Number) config.get(chave)).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private static Fila configurarFila(Map<String, Object> config, String nomeFila) {
        Map<String, Object> filaConfig = (Map<String, Object>) config.get(nomeFila);
        int servidores = ((Number) filaConfig.get("servidores")).intValue();
        int capacidade = ((Number) filaConfig.get("capacidade")).intValue();
        double atendimentoMinimo = ((Number) filaConfig.get("atendimento_minimo")).doubleValue();
        double atendimentoMaximo = ((Number) filaConfig.get("atendimento_maximo")).doubleValue();
        return new Fila(nomeFila, servidores, capacidade, atendimentoMinimo, atendimentoMaximo);
    }

    private static double calcularPercentual(int parte, int total) {
        return total == 0 ? 0 : (parte / (double) total) * 100;
    }
}
