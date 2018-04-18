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
package br.com.carlosrafaelgn.navinha.jogo.elementos;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

import br.com.carlosrafaelgn.navinha.jogo.desenho.FolhaDeSprites;
import br.com.carlosrafaelgn.navinha.jogo.desenho.Vista;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.interacao.Ponteiro;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.Vetor;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeModelo;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeTextura;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public final class Nave extends AlvoDeTiro {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int VIDAS_INICIAIS = 10;

	private static final float VELOCIDADE_EM_UNIDADES_POR_SEGUNDO = 8.0f;
	private static final float ACELERACAO_EM_UNIDADES_POR_SEGUNDO_2 = 24.0f;

	private static final float INTERVALO_ENTRE_TIROS = 0.4f;

	private static final int MOVIMENTO_PARADO = 0;
	private static final int MOVIMENTO_PARA_ESQUERDA = 1;
	private static final int MOVIMENTO_PARA_DIREITA = 2;

	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	public interface Observador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void vidaDaNaveAlterada(Nave nave);
	}

	private static abstract class Controle extends Recurso {
		//----------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//----------------------------------------------------------------------------------------------

		public abstract boolean isSuportado();

		//----------------------------------------------------------------------------------------------
		// Métodos públicos
		//----------------------------------------------------------------------------------------------

		public abstract int determineTipoDeMovimento();
	}

	private static final class ControlePorToque extends Controle {
		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		@Override
		public boolean isSuportado() {
			// O controle por toque sempre é suportado
			return true;
		}

		@Override
		public boolean isCarregado() {
			// Não utilizamos recursos internos!
			return false;
		}

		//------------------------------------------------------------------------------------------
		// Métodos privados e protegidos
		//------------------------------------------------------------------------------------------

		@Override
		protected void carregueInternamente() {
			// Nada a fazer!
		}

		@Override
		protected void libereInternamente() {
			// Nada a fazer!
		}

		@Override
		protected void destruaInternamente() {
			// Nada a fazer!
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public int determineTipoDeMovimento() {
			// Apenas determina qual o tipo de movimento da nave com base no estado de todos os
			// ponteiros disponibilizados pelo jogo

			Jogo jogo = Jogo.getJogo();

			float metadeDaLarguraDaVista = 0.5f * Tela.getTela().getLarguraDaVista();

			int tipoDeMovimento = 0;

			Vetor<Ponteiro> ponteiros = jogo.getPonteiros();
			for (int i = ponteiros.comprimento() - 1; i >= 0; i--) {
				Ponteiro ponteiro = ponteiros.item(i);
				if (ponteiro.isPressionado()) {
					if (ponteiro.getX() < metadeDaLarguraDaVista) {
						tipoDeMovimento |= MOVIMENTO_PARA_ESQUERDA;
					} else {
						tipoDeMovimento |= MOVIMENTO_PARA_DIREITA;
					}
				}
			}

			return tipoDeMovimento;
		}
	}

	private static final class ControlePorMovimento extends Controle {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private Thread threadDeCapturaDoSensor;
		private Looper looperDaThreadDeCapturaDoSensor;
		private volatile boolean threadDeCapturaDoSensorPronta, suportado;
		private volatile int tipoDeMovimento;

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private Thread getThreadDeCapturaDoSensor() {
			return threadDeCapturaDoSensor;
		}

		private void setThreadDeCapturaDoSensor(Thread threadDeCapturaDoSensor) {
			this.threadDeCapturaDoSensor = threadDeCapturaDoSensor;
		}

		private Looper getLooperDaThreadDeCapturaDoSensor() {
			return looperDaThreadDeCapturaDoSensor;
		}

		private void setLooperDaThreadDeCapturaDoSensor(Looper looperDaThreadDeCapturaDoSensor) {
			this.looperDaThreadDeCapturaDoSensor = looperDaThreadDeCapturaDoSensor;
		}

		private boolean isThreadDeCapturaDoSensorPronta() {
			return threadDeCapturaDoSensorPronta;
		}

		private void setThreadDeCapturaDoSensorPronta(boolean threadDeCapturaDoSensorPronta) {
			this.threadDeCapturaDoSensorPronta = threadDeCapturaDoSensorPronta;
		}

		@Override
		public boolean isSuportado() {
			return suportado;
		}

		private void setSuportado(boolean suportado) {
			this.suportado = suportado;
		}

		private int getTipoDeMovimento() {
			return tipoDeMovimento;
		}

		private void setTipoDeMovimento(int tipoDeMovimento) {
			this.tipoDeMovimento = tipoDeMovimento;
		}

		@Override
		public boolean isCarregado() {
			// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
			// jeitos diferentes)
			return (getThreadDeCapturaDoSensor() != null);
		}

		//------------------------------------------------------------------------------------------
		// Métodos privados e protegidos
		//------------------------------------------------------------------------------------------

		private void valorDoAcelerometroAlterado(float[] valores) {
			// O referencial dos valores dos sensores é fixo, pois normalmente está preso ao
			// aparelho, mas como a tela é rotacionada, os eixos x e y são alterados na percepção
			// do jogador
			// Apesar do valor do eixo y não ser utilizado, deixo ele aqui como referência para
			// quem precisar utilizar ;)

			float x;

			switch (Tela.getTela().getRotacao()) {
			case Tela.ROTACAO_90:
				x = -valores[1];
				// y = valores[0];
				break;
			case Tela.ROTACAO_180:
				x = -valores[0];
				// y = -valores[1];
				break;
			case Tela.ROTACAO_270:
				x = valores[1];
				// y = -valores[0];
				break;
			default: // Tela.ROTACAO_0
				x = valores[0];
				// y = valores[1];
				break;
			}

			// O valor normalmente lido do acelerômetro varia de aproximadamente -9,81 a 9,81 (m/s²)
			// que é a aceleração da gravidade no planeta Terra

			// Utilizaremos o conceito de deadzone para evitar movimentos irregulares
			switch (getTipoDeMovimento()) {
			case MOVIMENTO_PARA_ESQUERDA:
				if (x > 1.0f) {
					setTipoDeMovimento(MOVIMENTO_PARA_ESQUERDA);
				} else if (x > -0.5f) {
					setTipoDeMovimento(MOVIMENTO_PARADO);
				}
				break;
			case MOVIMENTO_PARA_DIREITA:
				if (x < -1.0f) {
					setTipoDeMovimento(MOVIMENTO_PARA_DIREITA);
				} else if (x < 0.5f) {
					setTipoDeMovimento(MOVIMENTO_PARADO);
				}
				break;
			default:
				if (x < -1.0f) {
					setTipoDeMovimento(MOVIMENTO_PARA_DIREITA);
				} else if (x > 1.0f) {
					setTipoDeMovimento(MOVIMENTO_PARA_ESQUERDA);
				}
				break;
			}
		}

		private void executeCaptura() {
			SensorManager sensorManager = (SensorManager)Jogo.getJogo().getContext().getSystemService(Context.SENSOR_SERVICE);
			SensorEventListener sensorEventListener = new SensorEventListener() {
				@Override
				public void onSensorChanged(SensorEvent event) {
					valorDoAcelerometroAlterado(event.values);
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					// Nada a fazer!
				}
			};

			// Prepara um looper nessa thread para que ela possa receber e processar os eventos
			// enviados pelo Android
			Looper.prepare();

			setLooperDaThreadDeCapturaDoSensor(Looper.myLooper());

			try {
				Sensor acelerometro;
				// Vamos começar pedindo o sensor de gravidade padrão do Android, já que para nosso
				// uso ele terá o mesmo efeito do acelerômetro convencional, com a vantagem de
				// entregar valores que oscilam menos
				try {
					acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
				} catch (Exception ex) {
					acelerometro = null;
				}

				if (acelerometro == null) {
					// Como não foi possível obter um sensor de gravidade, vamos tentar pedir um
					// acelerômetro convencional, mesmo
					acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				}

				setSuportado(acelerometro != null);

				// Tudo pronto, hora de liberar a outra thread
				synchronized (this) {
					setThreadDeCapturaDoSensorPronta(true);
					notify();
				}

				if (acelerometro == null) {
					// Nada a fazer, a não ser parar por aqui
					return;
				}

				Handler handler = new Handler(Looper.myLooper());

				// Registra o tratador dos eventos para o acelerômetro
				sensorManager.registerListener(sensorEventListener, acelerometro, SensorManager.SENSOR_DELAY_GAME, handler);

				// Inicia o processamento dos eventos nessa thread, para que possamos receber os
				// dados do acelerômetro
				Looper.loop();

			} catch (Exception ex) {
				// Já que algo deu errado, vamos liberar a outra thread
				synchronized (this) {
					setThreadDeCapturaDoSensorPronta(true);
					notify();
				}
			} finally {
				// No final das contas (com ou sem exceção) não podemos esquecer de remover o
				// registro do sensorEventListener
				sensorManager.unregisterListener(sensorEventListener);
			}
		}

		@Override
		protected void carregueInternamente() {
			// Vamos criar todos os objetos utilizados pelo controle por acelerômetro

			// Cria a thread que receberá, do Android, as notificações de novos dados do sensor
			Thread threadDeCapturaDoSensor = new Thread(new Runnable() {
				@Override
				public void run() {
					executeCaptura();
				}
			}, "Thread de captura do acelerômetro");

			// Inicialmente vamos supor que o sensor não é suportado, e a outra thread irá corrigir
			// essa premissa
			setThreadDeCapturaDoSensor(threadDeCapturaDoSensor);
			setSuportado(false);
			setThreadDeCapturaDoSensorPronta(false);

			threadDeCapturaDoSensor.start();

			// Vamos aguardar até que a inicialização da thread termine antes de continuarmos
			synchronized (this) {
				if (!isThreadDeCapturaDoSensorPronta()) {
					try {
						wait();
					} catch (InterruptedException ex) {
						// Nada a fazer, a não seguir em frente!
					}
				}
			}
		}

		@Override
		protected void libereInternamente() {
			// Vamos liberar toda a memória que não será mais utilizada

			// Para o looper, para que a thread possa encerrar
			Looper looperDaThreadDeCapturaDoSensor = getLooperDaThreadDeCapturaDoSensor();
			if (looperDaThreadDeCapturaDoSensor != null) {
				looperDaThreadDeCapturaDoSensor.quit();
				setLooperDaThreadDeCapturaDoSensor(null);
			}

			// Aguarda pelo término da thread
			Thread threadDeCapturaDoSensor = getThreadDeCapturaDoSensor();
			if (threadDeCapturaDoSensor != null) {
				try {
					threadDeCapturaDoSensor.join();
				} catch (InterruptedException ex) {
					// Nada a fazer, a não seguir em frente!
				}
				setThreadDeCapturaDoSensor(null);
			}
		}

		@Override
		protected void destruaInternamente() {
			// Nada a fazer, pois o método libereInternamente() já faz toda a limpeza
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public int determineTipoDeMovimento() {
			// O trabalho difícil é feito lá na outra thread
			return getTipoDeMovimento();
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private int tipoDeMovimento, vidas;
	private float x, y, velocidade, velocidadeMaxima, aceleracaoMaxima, intervaloDesdeUltimoTiro;
	private boolean habilitada, controladaPorMovimento;
	private Controle controle;
	private Observador observador;
	private CoordenadasDeModelo coordenadasDeModelo;
	private CoordenadasDeModelo coordenadasDeModeloDosLimites;
	private CoordenadasDeTextura coordenadasDeTextura;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Nave(FolhaDeSprites folhaDeSprites, boolean controladaPorMovimento, Observador observador) {
		setFolhaDeSprites(folhaDeSprites);
		setVidas(VIDAS_INICIAIS);
		setControladaPorMovimento(controladaPorMovimento);
		setObservador(observador);

		carregueInternamente();

		// Define a posição inicial apenas depois de carregado
		Tela tela = Tela.getTela();

		setTipoDeMovimentoXY(MOVIMENTO_PARADO,
			0.5f * tela.getLarguraDaVista(),
			tela.getAlturaDaVista() - (folhaDeSprites.pixels(Vista.isEstreita(folhaDeSprites) ?
				1.0f :
				1.5f)));

		// Queremos que a nave possa participar do sistema de colisões, e seja detectada por outros
		// elementos de tela, como, por exemplo, um tiro
		setParteDoSistemaDeColisoes(true);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public FolhaDeSprites getFolhaDeSprites() {
		return folhaDeSprites;
	}

	private void setFolhaDeSprites(FolhaDeSprites folhaDeSprites) {
		this.folhaDeSprites = folhaDeSprites;
	}

	private int getTipoDeMovimento() {
		return tipoDeMovimento;
	}

	private void setTipoDeMovimento(int tipoDeMovimento) {
		if (getTipoDeMovimento() != tipoDeMovimento) {
			this.tipoDeMovimento = tipoDeMovimento;

			atualizeAreaLimiteECoordenadas();
		}
	}

	public int getVidas() {
		return vidas;
	}

	private void setVidas(int vidas) {
		if (vidas < 0) {
			throw new RuntimeException("Não é possível ter vidas negativas");
		}

		if (getVidas() == vidas) {
			// Não vamos avisar o observador se nada mudou
			return;
		}

		this.vidas = vidas;

		if (vidas == 0) {
			// Morremos!!!! Vamos indicar isso criando uma explosão no meio da nave
			getLista().adicioneAcima(new Explosao(getFolhaDeSprites(), true, getX(), getY()), this);

			removaEDestrua();
		}
		Observador observador = getObservador();
		if (observador != null) {
			observador.vidaDaNaveAlterada(this);
		}
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	private void setTipoDeMovimentoX(int tipoDeMovimento, float x) {
		if (getTipoDeMovimento() != tipoDeMovimento) {
			this.tipoDeMovimento = tipoDeMovimento;
			this.x = x;

			atualizeAreaLimiteECoordenadas();
		} else {
			this.x = x;

			atualizeAreaLimite();
		}
	}

	private void setTipoDeMovimentoXY(int tipoDeMovimento, float x, float y) {
		if (getTipoDeMovimento() != tipoDeMovimento) {
			this.tipoDeMovimento = tipoDeMovimento;
			this.x = x;
			this.y = y;

			atualizeAreaLimiteECoordenadas();
		} else {
			this.x = x;
			this.y = y;

			atualizeAreaLimite();
		}
	}

	private float getVelocidade() {
		return velocidade;
	}

	private void setVelocidade(float velocidade) {
		this.velocidade = velocidade;
	}

	private float getVelocidadeMaxima() {
		return velocidadeMaxima;
	}

	private void setVelocidadeMaxima(float velocidadeMaxima) {
		this.velocidadeMaxima = velocidadeMaxima;
	}

	private float getAceleracaoMaxima() {
		return aceleracaoMaxima;
	}

	private void setAceleracaoMaxima(float aceleracaoMaxima) {
		this.aceleracaoMaxima = aceleracaoMaxima;
	}

	private float getIntervaloDesdeUltimoTiro() {
		return intervaloDesdeUltimoTiro;
	}

	private void setIntervaloDesdeUltimoTiro(float intervaloDesdeUltimoTiro) {
		this.intervaloDesdeUltimoTiro = intervaloDesdeUltimoTiro;
	}

	public boolean isHabilitada() {
		return habilitada;
	}

	public void setHabilitada(boolean habilitada) {
		this.habilitada = habilitada;
	}

	public boolean isControladaPorMovimento() {
		return controladaPorMovimento;
	}

	public void setControladaPorMovimento(boolean controladaPorMovimento) {
		this.controladaPorMovimento = controladaPorMovimento;

		if (controladaPorMovimento) {
			setControle(new ControlePorMovimento());
		} else {
			setControle(new ControlePorToque());
		}
	}

	private Controle getControle() {
		return controle;
	}

	private void setControle(Controle controle) {
		Controle antigoControle = getControle();

		if (antigoControle != null) {
			antigoControle.destrua();
		}

		this.controle = controle;

		if (controle != null) {
			controle.carregue();

			// Caso o dispositivo não suporte esse controle, o controle por toque sempre é suportado
			if (!controle.isSuportado()) {
				controle.destrua();

				controle = new ControlePorToque();
				controle.carregue();

				this.controle = controle;
			}
		}
	}

	public Observador getObservador() {
		return observador;
	}

	public void setObservador(Observador observador) {
		this.observador = observador;
	}

	private CoordenadasDeModelo getCoordenadasDeModelo() {
		return coordenadasDeModelo;
	}

	private void setCoordenadasDeModelo(CoordenadasDeModelo coordenadasDeModelo) {
		this.coordenadasDeModelo = coordenadasDeModelo;
	}

	private CoordenadasDeModelo getCoordenadasDeModeloDosLimites() {
		return coordenadasDeModeloDosLimites;
	}

	private void setCoordenadasDeModeloDosLimites(CoordenadasDeModelo coordenadasDeModeloDosLimites) {
		this.coordenadasDeModeloDosLimites = coordenadasDeModeloDosLimites;
	}

	private CoordenadasDeTextura getCoordenadasDeTextura() {
		return coordenadasDeTextura;
	}

	private void setCoordenadasDeTextura(CoordenadasDeTextura coordenadasDeTextura) {
		this.coordenadasDeTextura = coordenadasDeTextura;
	}

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getCoordenadasDeModelo() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados
	//----------------------------------------------------------------------------------------------

	private void atire() {
		// Cria um tiro na parte de cima da nave, mas centralizado horizontalmente
		getLista().adicioneAcima(new Tiro(getFolhaDeSprites(), getX(), getAreaLimiteCima(), true), this);
	}

	private void atualizeAreaLimite() {
		altereAreaLimite(getCoordenadasDeModeloDosLimites(), getX(), getY());
	}

	private void atualizeAreaLimiteECoordenadas() {
		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();
		int tipoDeMovimento = getTipoDeMovimento();

		setCoordenadasDeModeloDosLimites(folhaDeSprites.getCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento(tipoDeMovimento));
		setCoordenadasDeTextura(folhaDeSprites.getCoordenadasDeTexturaDaNavePorTipoDeMovimento(tipoDeMovimento));

		atualizeAreaLimite();
	}

	@Override
	protected void carregueInternamente() {
		// A velocidade máxima, a aceleração máxima e as coordenadas de modelo não mudam

		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();

		setVelocidadeMaxima(folhaDeSprites.pixels(VELOCIDADE_EM_UNIDADES_POR_SEGUNDO));
		setAceleracaoMaxima(folhaDeSprites.pixels(ACELERACAO_EM_UNIDADES_POR_SEGUNDO_2));

		setCoordenadasDeModelo(folhaDeSprites.getCoordenadasDeModeloDaNave());

		atualizeAreaLimiteECoordenadas();

		getControle().carregue();
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		getControle().libere();

		setCoordenadasDeModelo(null);
		setCoordenadasDeModeloDosLimites(null);
		setCoordenadasDeTextura(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		Controle controle = getControle();
		if (controle != null) {
			controle.destrua();
			setControle(null);
		}

		setFolhaDeSprites(null);
		setObservador(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe ElementoDeTela, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	@Override
	protected void processeUmQuadroSemPausa(float deltaSegundos) {
		// Quando a nave está desabilitada, ela nem se move, nem atira
		if (!isHabilitada()) {
			return;
		}

		float larguraDaVista = Tela.getTela().getLarguraDaVista();

		// tipoDeMovimento deve ser tratado como um conjunto de bits, onde cada um dos dois
		// primeiros bits indicam quel é o tipo de movimento (MOVIMENTO_PARA_ESQUERDA,
		// MOVIMENTO_PARA_DIREITA, ambos ou nenhum)
		int tipoDeMovimento = getControle().determineTipoDeMovimento();

		// O único caso preocupante é quando o tipoDeMovimento indica tanto movimento para esquerda,
		// quanto para direita
		if (tipoDeMovimento == (MOVIMENTO_PARA_ESQUERDA | MOVIMENTO_PARA_DIREITA)) {
			// Se está indo para os dois lados, então não deve se mover
			tipoDeMovimento = MOVIMENTO_PARADO;
		}

		float velocidade = getVelocidade(), velocidadeMaxima;

		switch (tipoDeMovimento) {
		case MOVIMENTO_PARA_ESQUERDA:
			// A nave precisa ser acelerada para a esquerda (velocidade negativa)
			// v = v0 + a * t
			velocidade -= (getAceleracaoMaxima() * deltaSegundos);

			// Na vida real existem fatores, como por exemplo, a potência do motor, ou o atrito, que
			// limitam a velocidade máxima de um veículo... como isso não existe aqui, temos que
			// "fazer de conta", e limitar a velocidade na mão ;)
			velocidadeMaxima = -getVelocidadeMaxima();
			if (velocidade < velocidadeMaxima) {
				velocidade = velocidadeMaxima;
			}
			break;
		case MOVIMENTO_PARA_DIREITA:
			// Idem para a direita (velocidade positiva)
			velocidade += (getAceleracaoMaxima() * deltaSegundos);

			velocidadeMaxima = getVelocidadeMaxima();
			if (velocidade > velocidadeMaxima) {
				velocidade = velocidadeMaxima;
			}
			break;
		default:
			// A nave precisa ser "freada" e nós faremos isso mais rapidamente do que a aceleração
			// normal (por isso o fator de multiplicação na aceleração)
			if (velocidade > 0.0f) {
				// Quando a velocidade era positiva, nós subtraímos, e verificamos se ela ficou
				// abaixo de 0 (para detectar uma parada total)
				velocidade -= (2.0f * getAceleracaoMaxima() * deltaSegundos);
				if (velocidade < 0.0f) {
					velocidade = 0.0f;
				}
			} else if (velocidade < 0.0f) {
				// Quando a velocidade era negativa, nós adicionamos, e verificamos se ela ficou
				// acima de 0 (para detectar uma parada total)
				velocidade += (2.0f * getAceleracaoMaxima() * deltaSegundos);
				if (velocidade > 0.0f) {
					velocidade = 0.0f;
				}
			}
			break;
		}

		if (velocidade != 0.0f) {
			// s = s0 + v * t
			float x = getX() + (velocidade * deltaSegundos);

			// Quando a nave "bate" em um dos cantos da tela, ela para
			if (x < 0.0f) {
				x = 0.0f;
				velocidade = 0.0f;
			} else if (x > larguraDaVista) {
				x = larguraDaVista;
				velocidade = 0.0f;
			}

			setTipoDeMovimentoX(tipoDeMovimento, x);
		} else {
			setTipoDeMovimento(tipoDeMovimento);
		}

		setVelocidade(velocidade);

		// Será que é hora de atirar?
		float intervaloDesdeUltimoTiro = getIntervaloDesdeUltimoTiro() + deltaSegundos;
		if (intervaloDesdeUltimoTiro >= INTERVALO_ENTRE_TIROS) {
			atire();
			do {
				intervaloDesdeUltimoTiro -= INTERVALO_ENTRE_TIROS;
			} while (intervaloDesdeUltimoTiro >= INTERVALO_ENTRE_TIROS);
		}
		setIntervaloDesdeUltimoTiro(intervaloDesdeUltimoTiro);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void acertadoPorUmTiro(Tiro tiro) {
		int vidas = getVidas();

		// Prevenção simples, para o caso da nave receber dois tiros no mesmo quadro, e a vida,
		// antes de receber o primeiro tiro, valia 1
		if (vidas > 0) {
			if (vidas != 1) {
				// Se ainda não morremos, vamos fazer um efeito especial, para indicar que fomos
				// atingidos
				getLista().adicioneAcima(new Explosao(getFolhaDeSprites(), false, tiro.getX(), tiro.getY()), this);
			}
			setVidas(vidas - 1);
		}
	}

	@Override
	public void desenheUmQuadro() {
		Tela.getTela().desenhe(getFolhaDeSprites().getImagem(), getCoordenadasDeModelo(), 1.0f, getCoordenadasDeTextura(), getX(), getY());
	}
}
