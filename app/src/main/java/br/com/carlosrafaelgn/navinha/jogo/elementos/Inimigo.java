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
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDeQuadros;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.animacao.contadores.Contador;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDePontos;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDePontosComContador;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.VetorFloat;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeModelo;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Ponto;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.elemento.ListaDeElementosDeTela;

public final class Inimigo extends AlvoDeTiro {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	// Cada inimigo poderá se mover em velocidades diferentes
	public static final float VELOCIDADE_MINIMA = 0.075f;
	public static final float VELOCIDADE_MAXIMA = 0.25f;

	// 50% das vezes que um inimigo morrer, ele dará um tiro
	private static final int PERMILAGEM_DE_CHANCE_DE_ATIRAR_AO_EXPLODIR = 500;

	// Intervalo mínimo para que um inimigo atire (em milissegundos)
	private static final int INTERVALO_MINIMO_ENTRE_TIROS_EM_MS = 400;

	private static final float QUADROS_POR_SEGUNDO = 6.0f;

	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	public interface Observador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void inimigoExplodiu(Inimigo inimigo);
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private Nave nave;
	private HordaDeInimigos hordaDeInimigos;
	private InterpoladorDePontosComContador interpoladorDePontos;
	private InterpoladorDeQuadros interpoladorDeQuadros;
	private int vidas;
	private long horaDoUltimoTiro;
	private float x, y;
	private final float xEmDescansoNaHorda, yEmDescansoNaHorda;
	private CoordenadasDeModelo coordenadasDeModelo, coordenadasDeModeloDosLimites;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Inimigo(FolhaDeSprites folhaDeSprites, Nave nave, HordaDeInimigos hordaDeInimigos, InterpoladorDePontosComContador interpoladorDePontosInicial, int vidas, float xEmDescansoNaHorda, float yEmDescansoNaHorda) {
		setFolhaDeSprites(folhaDeSprites);
		setNave(nave);
		setHordaDeInimigos(hordaDeInimigos);
		setInterpoladorDePontos(interpoladorDePontosInicial);

		this.xEmDescansoNaHorda = xEmDescansoNaHorda;
		this.yEmDescansoNaHorda = yEmDescansoNaHorda;

		Jogo jogo = Jogo.getJogo();

		// Inicializa o horário do último tiro com um valor válido (em vez de 0)
		setHoraDoUltimoTiro(jogo.getHoraAnterior());

		InterpoladorDeQuadros interpoladorDeQuadros = new InterpoladorDeQuadros(nave.getFolhaDeSprites().getCoordenadasDeTexturaDoInimigoPorVida(vidas), Contador.LOOPING, QUADROS_POR_SEGUNDO);
		// Para que os inimigos não iniciem todos no mesmo quadro
		interpoladorDeQuadros.setIndiceDoQuadroAtual(jogo.numeroAleatorio(interpoladorDeQuadros.getContagemDeQuadros()));
		setInterpoladorDeQuadros(interpoladorDeQuadros);

		carregueInternamente();

		// Define a vida apenas depois de carregado (por causa das coordenadas de textura)
		setVidas(vidas);

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

	public Nave getNave() {
		return nave;
	}

	private void setNave(Nave nave) {
		this.nave = nave;
	}

	public HordaDeInimigos getHordaDeInimigos() {
		return hordaDeInimigos;
	}

	private void setHordaDeInimigos(HordaDeInimigos hordaDeInimigos) {
		this.hordaDeInimigos = hordaDeInimigos;
	}

	private InterpoladorDePontosComContador getInterpoladorDePontos() {
		return interpoladorDePontos;
	}

	private void setInterpoladorDePontos(InterpoladorDePontosComContador interpoladorDePontos) {
		this.interpoladorDePontos = interpoladorDePontos;
	}

	private InterpoladorDeQuadros getInterpoladorDeQuadros() {
		return interpoladorDeQuadros;
	}

	private void setInterpoladorDeQuadros(InterpoladorDeQuadros interpoladorDeQuadros) {
		this.interpoladorDeQuadros = interpoladorDeQuadros;
	}

	public int getVidas() {
		return vidas;
	}

	private void setVidas(int vidas) {
		if (vidas < 0) {
			throw new RuntimeException("Não é possível ter vidas negativas");
		}

		this.vidas = vidas;

		if (vidas == 0) {
			// Explodiu!!!! Vamos indicar isso criando uma explosão no meio do inimigo
			ListaDeElementosDeTela listaDeElementosDeTela = getLista();
			listaDeElementosDeTela.adicioneAcima(new Explosao(getFolhaDeSprites(), false, getX(), getY()), this);

			// Além disso, algumas vezes, ele dará um tiro ao explodir ;)
			if (Jogo.getJogo().sorteie(PERMILAGEM_DE_CHANCE_DE_ATIRAR_AO_EXPLODIR)) {
				atire();
			}

			HordaDeInimigos hordaDeInimigos = getHordaDeInimigos();
			if (hordaDeInimigos != null) {
				hordaDeInimigos.inimigoExplodiu(this);
			}

			// Nós podemos remover o inimigo da lista somente depois de adicionados todos os efeitos
			removaEDestrua();
		} else {
			// Nosso desenho mudou (de verde para amarelo ou de amarelo para vermelho)
			atualizeCoordenadasDeTextura();
		}
	}

	private long getHoraDoUltimoTiro() {
		return horaDoUltimoTiro;
	}

	private void setHoraDoUltimoTiro(long horaDoUltimoTiro) {
		this.horaDoUltimoTiro = horaDoUltimoTiro;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	private void setXY(float x, float y) {
		this.x = x;
		this.y = y;

		atualizeAreaLimite();
	}

	public float getXEmDescansoNaHorda() {
		return xEmDescansoNaHorda;
	}

	public float getYEmDescansoNaHorda() {
		return yEmDescansoNaHorda;
	}

	public float getXEmDescanso() {
		return getXEmDescansoNaHorda() + getHordaDeInimigos().getX();
	}

	public float getYEmDescanso() {
		return getYEmDescansoNaHorda() + getHordaDeInimigos().getY();
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

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getCoordenadasDeModelo() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private void atualizeAreaLimite() {
		altereAreaLimite(getCoordenadasDeModeloDosLimites(), getX(), getY());
	}

	private void atualizeCoordenadasDeTextura() {
		int vidas = getVidas();
		if (vidas > 0) {
			getInterpoladorDeQuadros().setQuadros(getFolhaDeSprites().getCoordenadasDeTexturaDoInimigoPorVida(vidas));
		}
	}

	private void atualizeXY() {
		// Se existe um interpolador de pontos, a posição do inimigo virá dele, caso contrário,
		// a posição será sua posição de descanso dentro da horda inteira

		if (getInterpoladorDePontos() == null) {
			setXY(getXEmDescanso(), getYEmDescanso());
		}
	}

	@Override
	protected void carregueInternamente() {
		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();

		// As coordenadas de modelo e os limites não mudam
		setCoordenadasDeModelo(folhaDeSprites.getCoordenadasDeModeloDoInimigo());
		setCoordenadasDeModeloDosLimites(folhaDeSprites.getCoordenadasDeModeloDosLimitesDoInimigo());

		atualizeAreaLimite();

		atualizeCoordenadasDeTextura();
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		setCoordenadasDeModelo(null);
		setCoordenadasDeModeloDosLimites(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setFolhaDeSprites(null);
		setNave(null);
		setHordaDeInimigos(null);
		setInterpoladorDePontos(null);
		setInterpoladorDeQuadros(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe ElementoDeTela, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	@Override
	protected void processeUmQuadroSemPausa(float deltaSegundos) {
		// Calcula os quadros da animação do inimigo
		getInterpoladorDeQuadros().interpoleDelta(deltaSegundos);

		// Se o inimigo possui um interpolador de pontos, significa que ele está se movendo pela
		// tela, e precisamos atualizar sua posição
		InterpoladorDePontosComContador interpoladorDePontos = getInterpoladorDePontos();
		if (interpoladorDePontos != null) {
			Ponto ponto = interpoladorDePontos.interpoleDelta(deltaSegundos);
			if (interpoladorDePontos.getIntervalo() >= 1.0f) {
				// O movimento do inimigo acabou
				setInterpoladorDePontos(null);
				atualizeXY();
			} else {
				// Para garantir um retorno não muito brusco, os 25% finais serão dedicados ao
				// retorno suave do inimigo até sua posição de descanso
				// Isso ocorrerá da seguinte forma: inicialmente a posição do inimigo é controlada
				// apenas pelo interpolador de pontos, que pouco a pouco perde sua influência,
				// fazendo com que a posição do inimigo fique cada vez mais próxima de sua posição
				// de descanso dentro da horda
				if (interpoladorDePontos.getIntervalo() >= 0.5f) {
					// 0.8 se tornará 0
					// 1 continuará sendo 1
					// Os valores entre 0.8 e 1 serão linearmente mapeados para outros valores entre
					// 0 e 1
					float percentualDeRetorno = 2.0f * (interpoladorDePontos.getIntervalo() - 0.5f);

					setXY(
						(ponto.getX() * (1.0f - percentualDeRetorno)) +
						(getXEmDescanso() * percentualDeRetorno),

						(ponto.getY() * (1.0f - percentualDeRetorno)) +
						(getYEmDescanso() * percentualDeRetorno)
					);
				} else {
					setXY(ponto.getX(), ponto.getY());
				}
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void atire() {
		// Não deixa o inimigo atirar caso ele já tenha atirado há pouco tempo, ou caso a nave já
		// tenha explodido

		if (getNave().getVidas() == 0) {
			// A nave já havia explodido
			return;
		}

		long horaAnterior = Jogo.getJogo().getHoraAnterior();

		if ((horaAnterior - getHoraDoUltimoTiro()) >= INTERVALO_MINIMO_ENTRE_TIROS_EM_MS) {
			setHoraDoUltimoTiro(horaAnterior);

			// Cria um tiro no centro do inimigo
			getLista().adicioneAcima(new Tiro(getFolhaDeSprites(), getX(), getY(), false), this);
		}
	}

	public void voeAteANave() {
		if (getInterpoladorDePontos() != null) {
			// Nada a fazer por hora, pois o inimigo já está voando para algum lugar (ou para sua
			// posição de descanso, ou para a nave)
			return;
		}

		// Cria um caminho da posição atual até a nave, e de volta para cá, a não ser que a nave já
		// tenha explodido

		Nave nave = getNave();

		if (nave.getVidas() == 0) {
			// A nave já havia explodido
			return;
		}

		// Em vez de deixar os intervalos serem calculados automaticamente, o que geraria os
		// intervalos 0, 0.5 e 1, vamos gerar os intervalos manualmente, para fazer com que o
		// inimigo vá mais rápido até a nave, e volte mais devagar
		setInterpoladorDePontos(new InterpoladorDePontosComContador(InterpoladorDePontos.crieSpline(
				new VetorFloat(getX(), nave.getX()                                   , getX()),
				new VetorFloat(getY(), nave.getY() - getFolhaDeSprites().pixels(1.5f), getY()),
				new VetorFloat(0.0f  , 0.4f                                          , 1.0f)
			),
			Contador.UMA_VEZ,
			0.0f,
			Inimigo.VELOCIDADE_MAXIMA));
	}

	public void posicaoDaHordaMudou() {
		atualizeXY();
	}

	@Override
	public void acertadoPorUmTiro(Tiro tiro) {
		int vidas = getVidas();

		// Duas prevenções simples:
		// - caso o inimigo receba dois tiros no mesmo quadro, e a vida, antes de receber o primeiro
		// tiro, valia 1
		// - caso o inimigo receba um tiro enquanto estava acima da parte superior da tela, ou seja,
		// ainda estava entrando no jogo
		if (vidas > 0 && getY() > -getCoordenadasDeModeloDosLimites().getPivoY()) {
			if (vidas != 1) {
				// Se o inimigo ainda não explodiu, vamos fazer um efeito especial, para indicar que
				// foi atingido
				getLista().adicioneAcima(new Explosao(getFolhaDeSprites(), true, tiro.getX(), tiro.getY()), this);
			}
			setVidas(vidas - 1);
		}
	}

	@Override
	public void desenheUmQuadro() {
		Tela.getTela().desenhe(getFolhaDeSprites().getImagem(), getCoordenadasDeModelo(), 1.0f, getInterpoladorDeQuadros().getQuadroAtual(), getX(), getY());
	}
}
