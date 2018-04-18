//
// Navinha is distributed under the FreeBSD License
//
// Copyright (c) 2016, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.
//
// https://github.com/carlosrafaelgn/Navinha
//
package br.com.carlosrafaelgn.navinha.modelo.jogo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.util.Random;

import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.Vetor;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.interacao.Ponteiro;

public final class Jogo {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int MENSAGEM_ENCERRE = 1;

	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	public interface Agendador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void agendeParaOProximoQuadro(Runnable runnable);
	}

	public interface Observador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void jogoEncerrado();
	}

	private static class HandlerDaThreadPrincipal extends Handler {
		//----------------------------------------------------------------------------------------------
		// Construtores
		//----------------------------------------------------------------------------------------------

		public HandlerDaThreadPrincipal() {
			super(Looper.getMainLooper());
		}

		//----------------------------------------------------------------------------------------------
		// Métodos públicos
		//----------------------------------------------------------------------------------------------

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MENSAGEM_ENCERRE:
				Jogo.getJogo().encerre();
				break;
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	// A Tela utiliza o padrão Singleton, onde só existe uma instância do objeto no sistema inteiro
	// (perceba que para reforçar isso, o construtor da classe é privado)
	private static final Jogo jogo = new Jogo();

	private Context context;
	private Thread threadPrincipal;
	private HandlerDaThreadPrincipal handlerDaThreadPrincipal;
	private Random random;
	private volatile boolean encerrado, processando;
	private boolean interatividadeComJogadorValida;
	private Vetor<Ponteiro> ponteirosThreadPrincipal, ponteiros;
	private Cenario cenarioInicial, cenarioAtual, cenarioSeguinte;
	private Agendador agendador;
	private Observador observador;
	private long horaAnterior;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	private Jogo() {
		setRandom(new Random());
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public Context getContext() {
		return context;
	}

	private void setContext(Context context) {
		this.context = context;
	}

	public Thread getThreadPrincipal() {
		return threadPrincipal;
	}

	private void setThreadPrincipal(Thread threadPrincipal) {
		this.threadPrincipal = threadPrincipal;
	}

	public HandlerDaThreadPrincipal getHandlerDaThreadPrincipal() {
		return handlerDaThreadPrincipal;
	}

	private void setHandlerDaThreadPrincipal(HandlerDaThreadPrincipal handlerDaThreadPrincipal) {
		this.handlerDaThreadPrincipal = handlerDaThreadPrincipal;
	}

	private Random getRandom() {
		return random;
	}

	private void setRandom(Random random) {
		this.random = random;
	}

	public boolean isEncerrado() {
		return encerrado;
	}

	private void setEncerrado(boolean encerrado) {
		this.encerrado = encerrado;
	}

	public boolean isProcessando() {
		return processando;
	}

	private void setProcessando(boolean processando) {
		this.processando = processando;
	}

	public boolean isInteratividadeComJogadorValida() {
		return interatividadeComJogadorValida;
	}

	private void setInteratividadeComJogadorValida(boolean interatividadeComJogadorValida) {
		this.interatividadeComJogadorValida = interatividadeComJogadorValida;
	}

	public Vetor<Ponteiro> getPonteirosThreadPrincipal() {
		return ponteirosThreadPrincipal;
	}

	private void setPonteirosThreadPrincipal(Vetor<Ponteiro> ponteirosThreadPrincipal) {
		this.ponteirosThreadPrincipal = ponteirosThreadPrincipal;
	}

	public Vetor<Ponteiro> getPonteiros() {
		return ponteiros;
	}

	public Ponteiro getPonteiroPrincipal() {
		return ponteiros.item(0);
	}

	private void setPonteiros(Vetor<Ponteiro> ponteiros) {
		this.ponteiros = ponteiros;
	}

	private Cenario getCenarioInicial() {
		return cenarioInicial;
	}

	private void setCenarioInicial(Cenario cenarioInicial) {
		this.cenarioInicial = cenarioInicial;
	}

	public Cenario getCenarioAtual() {
		return cenarioAtual;
	}

	private void setCenarioAtual(Cenario cenarioAtual) {
		Cenario antigoCenarioAtual = getCenarioAtual();

		if (antigoCenarioAtual != null) {
			antigoCenarioAtual.destrua();
		}

		this.cenarioAtual = cenarioAtual;

		if (cenarioAtual != null) {
			cenarioAtual.inicie();
			cenarioAtual.carregue();
		}
	}

	public Cenario getCenarioSeguinte() {
		return cenarioSeguinte;
	}

	public void setCenarioSeguinte(Cenario cenarioSeguinte) {
		// Checagem básica
		if (Thread.currentThread() == getThreadPrincipal()) {
			throw new RuntimeException("O método setCenarioSeguinte() não deve ser executado na thread principal");
		}

		Cenario antigoCenarioSeguinte = getCenarioSeguinte();

		// Prevenção para o caso de nos pedirem para definir o cenário seguinte duas ou mais vezes
		if (antigoCenarioSeguinte != null && antigoCenarioSeguinte != cenarioSeguinte && antigoCenarioSeguinte != getCenarioAtual()) {
			antigoCenarioSeguinte.destrua();
		}

		this.cenarioSeguinte = cenarioSeguinte;
	}

	private Agendador getAgendador() {
		return agendador;
	}

	private void setAgendador(Agendador agendador) {
		this.agendador = agendador;
	}

	public Observador getObservador() {
		return observador;
	}

	private void setObservador(Observador observador) {
		this.observador = observador;
	}

	public long getHoraAnterior() {
		return horaAnterior;
	}

	private void setHoraAnterior(long horaAnterior) {
		this.horaAnterior = horaAnterior;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public static Jogo getJogo() {
		return jogo;
	}

	public String texto(int idDoRecurso) {
		return getContext().getText(idDoRecurso).toString();
	}

	public boolean sorteie(int permilagemDeAceite) {
		// Retorna true permilagemDeAceite / 1000 das vezes
		return (((getRandom().nextInt() & 0x7fffffff) % 1000) < permilagemDeAceite);
	}

	public int numeroAleatorio(int maximo) {
		// Retorna um número >= 0 e < maximo
		return getRandom().nextInt(maximo);
	}

	public float numeroAleatorio(float maximo) {
		// Retorna um número >= 0 e < maximo
		return (getRandom().nextFloat() * maximo);
	}

	public float numeroAleatorio(float minimo, float maximo) {
		// Retorna um número >= minimo e < maximo
		return (getRandom().nextFloat() * (maximo - minimo)) + minimo;
	}

	public void prepare(Context context, Agendador agendador, Observador observador, Cenario cenarioInicial) {
		// Objetos da API do Android
		setContext(context);
		setThreadPrincipal(Thread.currentThread());
		setHandlerDaThreadPrincipal(new HandlerDaThreadPrincipal());
		setAgendador(agendador);
		setObservador(observador);

		// Iniciamos parados
		setEncerrado(false);
		setProcessando(false);

		// Somente 4 ponteiros serão suportados
		setPonteirosThreadPrincipal(new Vetor<>(
			new Ponteiro(0),
			new Ponteiro(1),
			new Ponteiro(2),
			new Ponteiro(3)
		));

		setPonteiros(new Vetor<>(
			new Ponteiro(0),
			new Ponteiro(1),
			new Ponteiro(2),
			new Ponteiro(3)
		));

		setCenarioInicial(cenarioInicial);
	}

	public void interatividadeComJogadorPerdida() {
		// Checagem básica
		if (Thread.currentThread() == getThreadPrincipal()) {
			throw new RuntimeException("O método interatividadeComJogadorPerdida() não deve ser executado na thread principal");
		}

		Cenario cenarioAtual = getCenarioAtual();

		if (isInteratividadeComJogadorValida()) {
			setInteratividadeComJogadorValida(false);

			// Avisa o cenário atual sobre a perda da interatividade
			if (cenarioAtual != null) {
				cenarioAtual.interatividadeComJogadorPerdida();
			}
		}

		// Libera os recursos e a memória que o cenário estava utilizando
		if (cenarioAtual != null) {
			if (isEncerrado()) {
				setCenarioAtual(null);
			} else {
				cenarioAtual.libere();
			}
		}

		// Não é necessário nos preocupar com cenarioSeguinte e com recursosDoCenarioSeguinte, pois
		// eles *sempre* são null depois do término do método processeEDesenheUmQuadro()

		// Libera toda a memória e recursos usados pela tela
		Tela.getTela().destrua();

		// *** É possível que a classe GLSurfaceView já tenha liberado todos os recursos utilizados
		// pelo OpenGL, antes desse método ter sido chamado!
		// Isso pode acabar gerando o seguinte alerta para quem estiver monitorando o console:
		// call to OpenGL ES API with no current context
		// Apesar de ser um alerta, ele pode ser ignorado no nosso caso (onde estamos limpando tudo)
		// Esse alerta, contudo, deve ser observado e é preocupante, quando exibido durante a
		// execução do jogo
		// Mesmo sabendo que os recursos do OpenGL já tenham sido liberados, libereRecursos() é
		// executado para liberar *nossos* recursos :)
	}

	public void interatividadeComJogadorRecuperada(int larguraDaTela, int alturaDaTela) {
		// Checagem básica
		if (Thread.currentThread() == getThreadPrincipal()) {
			throw new RuntimeException("O método interatividadeComJogadorRecuperada() não deve ser executado na thread principal");
		}

		// Será que podemos mesmo prosseguir?
		if (isEncerrado()) {
			return;
		}

		setProcessando(true);

		Cenario cenarioAtual = getCenarioAtual();

		if (!Tela.getTela().telaAlterada(getContext(), larguraDaTela, alturaDaTela)) {
			// Como já possuíamos uma tela válida, e nada mudou, vamos apenas verificar se é
			// necessário recriar os recursos do cenário atual
			cenarioAtual.carregue();
		} else {
			Cenario cenarioInicial = getCenarioInicial();
			if (cenarioInicial != null) {
				// Se essa for mesmo a primeira vez que estamos iniciando, vamos definir o cenário
				// atual como sendo o cenário inicial
				setCenarioInicial(null);
				setCenarioAtual(cenarioInicial);
				cenarioAtual = cenarioInicial;
			} else {
				// Força a recriação dos recursos do cenário atual, já que a tela mudou
				cenarioAtual.libere();
				cenarioAtual.carregue();
			}
		}

		if (!isInteratividadeComJogadorValida()) {
			setInteratividadeComJogadorValida(true);

			// Avisa o cenário atual sobre a recuperação da interatividade
			cenarioAtual.interatividadeComJogadorRecuperada();
		}

		// Atualiza a marcação do horário para controlar o deltaMilissegundos dentro do método
		// processeEDesenheUmQuadro()
		setHoraAnterior(SystemClock.uptimeMillis());
	}

	public void pauseOProcessamento() {
		// Checagem básica
		if (Thread.currentThread() != getThreadPrincipal()) {
			throw new RuntimeException("O método pauseOProcessamento() deve ser executado na thread principal");
		}

		// O método setProcessando() não é público porque apenas a classe Jogo pode fazer com que
		// processando volte a valer true
		setProcessando(false);
	}

	public void agendeParaOProximoQuadro(Runnable runnable) {
		// Checagem básica
		if (Thread.currentThread() != getThreadPrincipal()) {
			throw new RuntimeException("O método crieTratadorDoBotaoVoltar() deve ser executado na thread principal");
		}

		// Tenta agendar esse runnable para executar na própria thread do jogo, no próximo quadro
		Agendador agendador = getAgendador();
		if (agendador != null) {
			agendador.agendeParaOProximoQuadro(runnable);
		}
	}

	public void botaoVoltarPressionado() {
		// Checagem básica
		if (Thread.currentThread() != getThreadPrincipal()) {
			throw new RuntimeException("O método botaoVoltarPressionado() deve ser executado na thread principal");
		}

		// Faz com que o jogo possa tratar o evento do pressionamento do botão voltar na própria
		// thread do jogo
		agendeParaOProximoQuadro(new Runnable() {
			@Override
			public void run() {
				Cenario cenarioAtual = getCenarioAtual();
				if (cenarioAtual != null) {
					cenarioAtual.botaoVoltarPressionado();
				}
			}
		});
	}

	public void processeEDesenheUmQuadro() {
		// Em prol do desempenho, a checagem básica de threads não é feita aqui

		// Será que podemos mesmo executar?
		if (!isProcessando()) {
			return;
		}

		// Vamos calcular quanto tempo efetivamente se passou desde o último quadro
		long horaAtual = SystemClock.uptimeMillis();

		// Quanto tempo se passou desde o último quadro?
		int deltaMilissegundos = (int)(horaAtual - getHoraAnterior());

		// É aqui que controlaremos a taxa de quadros por segundo (FPS) efetivamente percebida
		// pelo jogador
		if (deltaMilissegundos > 50) {
			// Muito tempo (mais de 50 ms) se passou desde o último quadro, o que pode
			// significar duas coisas:
			// - o sistema está realmente lento, possivelmente porque esteja executando muitos
			// outros aplicativos
			// - o processamento do nosso quadro está muito lento
			// De qualquer forma, vamos limitar o tempo a 50 ms, o que, apesar de fazer com que
			// o jogo pareça estar "travando", é melhor do que ter que lidar com intervalos de
			// tempo muito grandes (ao limitar a 50 ms, temos 20 quadros por segundo)
			deltaMilissegundos = 50;
		} else if (deltaMilissegundos < 1) {
			// 0??? Bem, nesse caso não podemos continuar, mesmo!
			return;
		}

		// Atualiza o relógio para o próximo quadro, afinal, hoje será "o ontem" de amanhã ;)
		setHoraAnterior(horaAtual);

		// Antes de processar o quadro, é preciso copiar as informações dos ponteiros da thread
		// principal para os ponteiros utilizados pelo processamento do jogo
		Vetor<Ponteiro> ponteirosThreadPrincipal = getPonteirosThreadPrincipal();
		Vetor<Ponteiro> ponteiros = getPonteiros();
		for (int i = ponteiros.comprimento() - 1; i >= 0; i--) {
			ponteiros.item(i).copie(ponteirosThreadPrincipal.item(i));
		}

		// Processa e desenha o quadro atual do jogo
		getCenarioAtual().processeEDesenheUmQuadro((float)deltaMilissegundos / 1000.0f);

		Tela.getTela().termineOQuadro();

		// Caso alguém tenha pedido para mudar o cenário
		Cenario cenarioSeguinte = getCenarioSeguinte();
		if (cenarioSeguinte != null) {
			setCenarioAtual(cenarioSeguinte);
			setCenarioSeguinte(null);
		}
	}

	public void encerre() {
		// Se esse método não for chamado da thread principal, encaminha uma mensagem para ela,
		// fazendo com que o método encerre seja chamado de lá
		if (Thread.currentThread() != getThreadPrincipal()) {
			getHandlerDaThreadPrincipal().sendEmptyMessage(MENSAGEM_ENCERRE);
			return;
		}

		// Indica que o jogo encerrou, e que ele está pausado, prevenindo a execução dos métodos
		// processeEDesenheUmQuadro() e telaPronta()
		setEncerrado(true);
		setProcessando(false);

		// Não é mais possível agendar runnables
		setAgendador(null);

		// Não se deve chamar setCenarioAtual(null) ou Tela.getTela().destrua() aqui, pois o método
		// encerre é chamado da thread principal do programa, enquanto que todos os recursos do
		// OpenGL foram criados na outra thread

		// Hora de avisar o observador
		Observador observador = getObservador();
		if (observador != null) {
			setObservador(null);
			observador.jogoEncerrado();
		}
	}
}
