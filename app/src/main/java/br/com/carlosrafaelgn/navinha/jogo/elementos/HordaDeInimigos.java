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

import br.com.carlosrafaelgn.navinha.jogo.desenho.FolhaDeSprites;
import br.com.carlosrafaelgn.navinha.jogo.desenho.Vista;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.animacao.contadores.Contador;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDePontos;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDePontosComContador;
import br.com.carlosrafaelgn.navinha.modelo.dados.ListaEmbaralhavel;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.VetorFloat;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.elemento.ListaDeElementosDeTela;

public final class HordaDeInimigos implements Inimigo.Observador {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	// Como o intervalo do interpolador vai de 0 até 1, 0.1 significa 10% do total por segundo
	private static final float INCREMENTO_POR_SEGUNDO_DO_MOVIMENTO = 0.1f;

	// Intervalos de tempo onde algum inimigo tentará atirar (com sua change de sucesso)
	private static final float INTERVALO_PARA_ATIRAR_25 = 1.0f;
	private static final float INTERVALO_PARA_ATIRAR_50 = 1.5f;
	private static final float INTERVALO_PARA_ATIRAR_75 = 2.5f;
	private static final float INTERVALO_PARA_ATIRAR_100 = 3.5f;

	// Intervalos de tempo entre os voos dos inimigos até a nave
	private static final float INTERVALO_PARA_VOAR_ATE_A_NAVE_1 = 2.0f;
	private static final float INTERVALO_PARA_VOAR_ATE_A_NAVE_2 = 3.0f;

	private static final float DEFASAGEM_NA_FILA = 0.055f;

	private static final int LINHAS_DE_INIMIGOS = 4;
	private static final int INIMIGOS_POR_LINHA = 10;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private Inimigo.Observador observador;
	private ListaEmbaralhavel<Inimigo> inimigos;
	private InterpoladorDePontosComContador interpoladorDePontos;
	private int indiceDoProximoATentarAtirar;
	private float intervaloDosTiros;
	private boolean intervaloDoVoo2Processado, intervaloDoVoo1Processado, intervaloDos75Processado, intervaloDos50Processado, intervaloDos25Processado;
	private final boolean entradaAleatoria;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public HordaDeInimigos(Nave nave, Inimigo.Observador observador, boolean entradaAleatoria) {
		setObservador(observador);
		setInimigos(new ListaEmbaralhavel<Inimigo>(LINHAS_DE_INIMIGOS * INIMIGOS_POR_LINHA));

		this.entradaAleatoria = entradaAleatoria;

		Tela tela = Tela.getTela();
		float meioDaVista = 0.5f * tela.getLarguraDaVista();

		FolhaDeSprites folhaDeSprites = nave.getFolhaDeSprites();

		// Calcula o espacamento entre os inimigos
		float espacamento = folhaDeSprites.pixels(1.5f);

		// Esse interpolador de pontos será responsável por mover a horda de inimigos inteira, em um
		// movimento repetitivo (o ponto do interpolador indica a posição central da horda)
		setInterpoladorDePontos(new InterpoladorDePontosComContador(InterpoladorDePontos.crieSpline(
				// Coordenadas X
				new VetorFloat(meioDaVista       , meioDaVista - (2.0f * espacamento), meioDaVista + (2.0f * espacamento), meioDaVista), Vista.isEstreita(folhaDeSprites) ?
				// Coordenadas Y (para uma vista estreita)
				new VetorFloat(2.0f * espacamento, 2.5f * espacamento                , 2.5f * espacamento                , 2.0f * espacamento) :
				// Coordenadas Y (para uma vista normal)
				new VetorFloat(2.0f * espacamento, 3.0f * espacamento                , 3.0f * espacamento                , 2.0f * espacamento)
			),
			Contador.LOOPING,
			0.0f,
			INCREMENTO_POR_SEGUNDO_DO_MOVIMENTO));

		crieOsInimigos(nave, espacamento);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public Inimigo.Observador getObservador() {
		return observador;
	}

	public void setObservador(Inimigo.Observador observador) {
		this.observador = observador;
	}

	private ListaEmbaralhavel<Inimigo> getInimigos() {
		return inimigos;
	}

	private void setInimigos(ListaEmbaralhavel<Inimigo> inimigos) {
		this.inimigos = inimigos;
	}

	private InterpoladorDePontosComContador getInterpoladorDePontos() {
		return interpoladorDePontos;
	}

	private void setInterpoladorDePontos(InterpoladorDePontosComContador interpoladorDePontos) {
		this.interpoladorDePontos = interpoladorDePontos;
	}

	public int getInimigosRestantes() {
		return getInimigos().size();
	}

	private int getIndiceDoProximoATentarAtirar() {
		return indiceDoProximoATentarAtirar;
	}

	private void setIndiceDoProximoATentarAtirar(int indiceDoProximoATentarAtirar) {
		this.indiceDoProximoATentarAtirar = indiceDoProximoATentarAtirar;
	}

	private float getIntervaloDosTiros() {
		return intervaloDosTiros;
	}

	private void setIntervaloDosTiros(float intervaloDosTiros) {
		this.intervaloDosTiros = intervaloDosTiros;
	}

	private boolean isIntervaloDoVoo2Processado() {
		return intervaloDoVoo2Processado;
	}

	private void setIntervaloDoVoo2Processado(boolean intervaloDoVoo2Processado) {
		this.intervaloDoVoo2Processado = intervaloDoVoo2Processado;
	}

	private boolean isIntervaloDoVoo1Processado() {
		return intervaloDoVoo1Processado;
	}

	private void setIntervaloDoVoo1Processado(boolean intervaloDoVoo1Processado) {
		this.intervaloDoVoo1Processado = intervaloDoVoo1Processado;
	}

	private boolean isIntervaloDos75Processado() {
		return intervaloDos75Processado;
	}

	private void setIntervaloDos75Processado(boolean intervaloDos75Processado) {
		this.intervaloDos75Processado = intervaloDos75Processado;
	}

	private boolean isIntervaloDos50Processado() {
		return intervaloDos50Processado;
	}

	private void setIntervaloDos50Processado(boolean intervaloDos50Processado) {
		this.intervaloDos50Processado = intervaloDos50Processado;
	}

	private boolean isIntervaloDos25Processado() {
		return intervaloDos25Processado;
	}

	private void setIntervaloDos25Processado(boolean intervaloDos25Processado) {
		this.intervaloDos25Processado = intervaloDos25Processado;
	}

	public boolean isEntradaAleatoria() {
		return entradaAleatoria;
	}

	public float getX() {
		return getInterpoladorDePontos().getUltimoPontoInterpolado().getX();
	}

	public float getY() {
		return getInterpoladorDePontos().getUltimoPontoInterpolado().getY();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private void crieOsInimigos(Nave nave, float espacamento) {
		Jogo jogo = Jogo.getJogo();
		Tela tela = Tela.getTela();
		FolhaDeSprites folhaDeSprites = nave.getFolhaDeSprites();
		ListaDeElementosDeTela listaDeElementosDeTela = nave.getLista();
		ListaEmbaralhavel<Inimigo> inimigos = getInimigos();

		float larguraDaVista = tela.getLarguraDaVista();
		float alturaDaVista = tela.getAlturaDaVista();

		// Posição vertical da primeira linha de inimigos, relativa ao centro da horda
		float y = -espacamento;

		if (isEntradaAleatoria()) {
			// Com a entrada aleatória os inimigos surgem de algum ponto da parte superior da tela,
			// vão até outro ponto na parte inferior, terminando na posição de descanso

			for (int linha = 0; linha < LINHAS_DE_INIMIGOS; linha++) {
				// Posição horizontal do primeiro inimigo a linha, relativa ao centro da horda
				float x = (float)(-INIMIGOS_POR_LINHA / 2) * espacamento;

				for (int i = 0; i < INIMIGOS_POR_LINHA; i++) {
					InterpoladorDePontosComContador interpoladorDePontos = new InterpoladorDePontosComContador(InterpoladorDePontos.crieAceleradoDesacelerado(
							new VetorFloat(jogo.numeroAleatorio(larguraDaVista), jogo.numeroAleatorio(larguraDaVista), getX() + x),
							new VetorFloat(-espacamento                        , alturaDaVista + espacamento         , getY() + y)
						),
						Contador.UMA_VEZ,
						0.0f,
						jogo.numeroAleatorio(Inimigo.VELOCIDADE_MINIMA, Inimigo.VELOCIDADE_MAXIMA));

					// Os inimigos mais atrás terão menos vida, enquanto que os da frente terão mais
					Inimigo inimigo = new Inimigo(folhaDeSprites, nave, this, interpoladorDePontos, linha == 0 ? 1 : linha, x, y);

					// Armazena o inimigo recém-criado para posteriormente controlarmos a quantidade
					// de inimigos na tela
					inimigos.add(inimigo);

					// Todos os inimigos devem aparecer acima da nave, mas abaixo dos demais
					// elementos de tela
					listaDeElementosDeTela.adicioneAcima(inimigo, nave);

					// Avança para o próximo inimigo
					x += espacamento;

					// Deixa um pequeno vão entre as duas metades
					if (i == (INIMIGOS_POR_LINHA / 2) - 1) {
						x += espacamento;
					}
				}

				// Avança para a próxima linha
				y += espacamento;
			}
		} else {
			// Com a entrada normal os inimigos surgem de dois pontos na parte superior, em fila,
			// vão até outro ponto na parte oposta, e terminam na posição de descanso

			VetorFloat vetorXEsquerda = new VetorFloat(larguraDaVista + espacamento        , 0.5f * larguraDaVista, (0.5f * larguraDaVista) - espacamento, (0.5f * larguraDaVista) - (3.0f * espacamento));
			VetorFloat vetorXDireita  = new VetorFloat(-espacamento                        , 0.5f * larguraDaVista, (0.5f * larguraDaVista) + espacamento, (0.5f * larguraDaVista) + (3.0f * espacamento));
			VetorFloat vetorY         = new VetorFloat(alturaDaVista - (3.0f * espacamento), 0.5f * alturaDaVista , espacamento                          , 3.0f * espacamento);

			float defasagem = 0.0f;

			for (int linha = 0; linha < LINHAS_DE_INIMIGOS; linha++) {
				// Posição horizontal do primeiro inimigo a linha, relativa ao centro da horda
				float xEsquerda = (float)(-INIMIGOS_POR_LINHA / 2) * espacamento;

				// Posição horizontal do último inimigo da linha, relativa ao centro da horda (o
				// pequeno vão que é deixado entre os inimigos é garantido aqui, ao utilizar
				// (INIMIGOS_POR_LINHA / 2) em vez de (INIMIGOS_POR_LINHA / 2) - 1)
				float xDireita = (float)(INIMIGOS_POR_LINHA / 2) * espacamento;

				for (int i = 0; i < (INIMIGOS_POR_LINHA / 2); i++) {
					//
					// Inimigo que vai para a esquerda
					//
					InterpoladorDePontosComContador interpoladorDePontos = new InterpoladorDePontosComContador(InterpoladorDePontos.crieSpline(
							vetorXEsquerda,
							vetorY
						),
						Contador.UMA_VEZ,
						defasagem,
						Inimigo.VELOCIDADE_MAXIMA);

					// Os inimigos mais atrás terão menos vida, enquanto que os da frente terão mais
					Inimigo inimigo = new Inimigo(folhaDeSprites, nave, this, interpoladorDePontos, linha == 0 ? 1 : linha, xEsquerda, y);

					// Armazena o inimigo recém-criado para posteriormente controlarmos a quantidade
					// de inimigos na tela
					inimigos.add(inimigo);

					// Todos os inimigos devem aparecer acima da nave, mas abaixo dos demais
					// elementos de tela
					listaDeElementosDeTela.adicioneAcima(inimigo, nave);

					// A defasagem controla o tempo que cada inimigo fica acima da parte superior da
					// tela, antes de entrar na área de jogo
					defasagem += DEFASAGEM_NA_FILA;

					//
					// Inimigo que vai para a direita (ver comentários acima)
					//
					interpoladorDePontos = new InterpoladorDePontosComContador(InterpoladorDePontos.crieSpline(
							vetorXDireita,
							vetorY
						),
						Contador.UMA_VEZ,
						defasagem,
						Inimigo.VELOCIDADE_MAXIMA);
					inimigo = new Inimigo(folhaDeSprites, nave, this, interpoladorDePontos, linha == 0 ? 1 : linha, xDireita, y);
					inimigos.add(inimigo);
					listaDeElementosDeTela.adicioneAcima(inimigo, nave);
					defasagem += DEFASAGEM_NA_FILA;

					// Avança para o próximo inimigo
					xEsquerda += espacamento;
					xDireita -= espacamento;
				}

				// Avança para a próxima linha
				y += espacamento;
			}
		}

		// Embaralha os inimigos (para fazer com que a ordem dos tiros seja sempre uma surpresa)
		inimigos.embaralhe();
	}

	private void atire() {
		ListaEmbaralhavel<Inimigo> inimigos = getInimigos();
		int indiceDoProximoATentarAtirar = getIndiceDoProximoATentarAtirar();

		if (indiceDoProximoATentarAtirar >= inimigos.size()) {
			// Se já passamos por todos os inimigos, hora de embaralhar a lista novamente
			indiceDoProximoATentarAtirar = 0;
			inimigos.embaralhe();
		}

		inimigos.get(indiceDoProximoATentarAtirar).atire();

		setIndiceDoProximoATentarAtirar(indiceDoProximoATentarAtirar + 1);
	}

	private void voe() {
		ListaEmbaralhavel<Inimigo> inimigos = getInimigos();
		int indiceDoProximoATentarAtirar = getIndiceDoProximoATentarAtirar();

		if (indiceDoProximoATentarAtirar >= inimigos.size()) {
			// Se já passamos por todos os inimigos, hora de embaralhar a lista novamente
			indiceDoProximoATentarAtirar = 0;
			inimigos.embaralhe();
		}

		inimigos.get(indiceDoProximoATentarAtirar).voeAteANave();

		// Diferente do método atire(), aqui não vamos incrementar o índice, para garantir que esse
		// mesmo inimigo acabe atirando depois :)
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void processeUmQuadro(float deltaSegundos) {
		// Move o centro da horda inteira
		getInterpoladorDePontos().interpoleDelta(deltaSegundos);

		// Avisa todos os inimigos sobre a mudança de posição da horda
		ListaEmbaralhavel<Inimigo> inimigos = getInimigos();
		for (int i = inimigos.size() - 1; i >= 0; i--) {
			inimigos.get(i).posicaoDaHordaMudou();
		}

		// Hora de tentar voar e atirar
		float intervaloDosInimigos = getIntervaloDosTiros() + deltaSegundos;

		if (intervaloDosInimigos >= INTERVALO_PARA_VOAR_ATE_A_NAVE_2) {
			// Sorteia antes de viajar (pode ser que ele não viaje)
			if (!isIntervaloDoVoo2Processado()) {
				setIntervaloDoVoo2Processado(true);

				if (Jogo.getJogo().sorteie(750)) {
					voe();
				}
			}
		} else if (intervaloDosInimigos >= INTERVALO_PARA_VOAR_ATE_A_NAVE_1) {
			// Sorteia antes de viajar (pode ser que ele não viaje)
			if (!isIntervaloDoVoo1Processado()) {
				setIntervaloDoVoo1Processado(true);

				if (Jogo.getJogo().sorteie(750)) {
					voe();
				}
			}
		}

		if (intervaloDosInimigos >= INTERVALO_PARA_ATIRAR_100) {
			// Reinicia o intervalo
			intervaloDosInimigos = 0.0f;

			setIntervaloDoVoo2Processado(false);
			setIntervaloDoVoo1Processado(false);
			setIntervaloDos75Processado(false);
			setIntervaloDos50Processado(false);
			setIntervaloDos25Processado(false);

			// Nesse caso não há sorteio
			atire();
		} else if (intervaloDosInimigos >= INTERVALO_PARA_ATIRAR_75) {
			// Sorteia antes de atirar (pode ser que não atire)
			if (!isIntervaloDos75Processado()) {
				setIntervaloDos75Processado(true);

				if (Jogo.getJogo().sorteie(750)) {
					atire();
				}
			}
		} else if (intervaloDosInimigos >= INTERVALO_PARA_ATIRAR_50) {
			// Sorteia antes de atirar (pode ser que não atire)
			if (!isIntervaloDos50Processado()) {
				setIntervaloDos50Processado(true);

				if (Jogo.getJogo().sorteie(500)) {
					atire();
				}
			}
		} else if (intervaloDosInimigos >= INTERVALO_PARA_ATIRAR_25) {
			// Sorteia antes de atirar (pode ser que não atire)
			if (!isIntervaloDos25Processado()) {
				setIntervaloDos25Processado(true);

				if (Jogo.getJogo().sorteie(250)) {
					atire();
				}
			}
		}

		setIntervaloDosTiros(intervaloDosInimigos);
	}

	@Override
	public void inimigoExplodiu(Inimigo inimigo) {
		// Remove esse inimigo do nosso conjunto e avisa o observador sobre a explosão que acabou de
		// ocorrer
		getInimigos().remove(inimigo);

		Inimigo.Observador observador = getObservador();
		if (observador != null) {
			observador.inimigoExplodiu(inimigo);
		}
	}

	public void destrua() {
		// Vamos invalidar o objeto e liberar toda a memória que não será mais utilizada

		ListaEmbaralhavel<Inimigo> inimigos = getInimigos();
		if (inimigos != null) {
			inimigos.clear();
			setInimigos(null);
		}

		setObservador(null);
		setInterpoladorDePontos(null);
	}
}
