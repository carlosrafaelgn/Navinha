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
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.Interpolador;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeModelo;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeTextura;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Imagem;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;

public final class Explosao extends ElementoDeTelaComPausa {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	public static final int CONTAGEM_DE_FRAGMENTOS = 75;
	private static final float VELOCIDADE_MINIMA = 10.0f;
	private static final float VELOCIDADE_MAXIMA = 80.0f;
	public static final float DURACAO_DA_ANIMACAO = 1.5f;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private Interpolador interpolador;
	private boolean explosaoDaNave;
	private float tempoDaAnimacao, xCentro, yCentro;
	private float[] x, y, seno, cosseno, velocidade;
	private CoordenadasDeModelo coordenadasDeModelo;
	private CoordenadasDeTextura coordenadasDeTextura;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Explosao(FolhaDeSprites folhaDeSprites, boolean explosaoDaNave, float xCentro, float yCentro) {
		setFolhaDeSprites(folhaDeSprites);
		setInterpolador(Interpolador.crieDesacelerado());
		setExplosaoDaNave(explosaoDaNave);
		setXCentro(xCentro);
		setYCentro(yCentro);

		carregueInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites getFolhaDeSprites() {
		return folhaDeSprites;
	}

	private void setFolhaDeSprites(FolhaDeSprites folhaDeSprites) {
		this.folhaDeSprites = folhaDeSprites;
	}

	private Interpolador getInterpolador() {
		return interpolador;
	}

	private void setInterpolador(Interpolador interpolador) {
		this.interpolador = interpolador;
	}

	private boolean isExplosaoDaNave() {
		return explosaoDaNave;
	}

	private void setExplosaoDaNave(boolean explosaoDaNave) {
		this.explosaoDaNave = explosaoDaNave;
	}

	private float getTempoDaAnimacao() {
		return tempoDaAnimacao;
	}

	private void setTempoDaAnimacao(float tempoDaAnimacao) {
		this.tempoDaAnimacao = tempoDaAnimacao;
	}

	private float getXCentro() {
		return xCentro;
	}

	private void setXCentro(float xCentro) {
		this.xCentro = xCentro;
	}

	private float getYCentro() {
		return yCentro;
	}

	private void setYCentro(float yCentro) {
		this.yCentro = yCentro;
	}

	private float[] getX() {
		return x;
	}

	private void setX(float[] x) {
		this.x = x;
	}

	private float[] getY() {
		return y;
	}

	private void setY(float[] y) {
		this.y = y;
	}

	private float[] getSeno() {
		return seno;
	}

	private void setSeno(float[] seno) {
		this.seno = seno;
	}

	private float[] getCosseno() {
		return cosseno;
	}

	private void setCosseno(float[] cosseno) {
		this.cosseno = cosseno;
	}

	private float[] getVelocidade() {
		return velocidade;
	}

	private void setVelocidade(float[] velocidade) {
		this.velocidade = velocidade;
	}

	private CoordenadasDeModelo getCoordenadasDeModelo() {
		return coordenadasDeModelo;
	}

	private void setCoordenadasDeModelo(CoordenadasDeModelo coordenadasDeModelo) {
		this.coordenadasDeModelo = coordenadasDeModelo;
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
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void carregueInternamente() {
		Jogo jogo = Jogo.getJogo();
		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();

		float[] x = new float[CONTAGEM_DE_FRAGMENTOS];
		float[] y = new float[CONTAGEM_DE_FRAGMENTOS];
		float[] seno = new float[CONTAGEM_DE_FRAGMENTOS];
		float[] cosseno = new float[CONTAGEM_DE_FRAGMENTOS];
		float[] velocidade = new float[CONTAGEM_DE_FRAGMENTOS];

		float xCentro = getXCentro();
		float yCentro = getYCentro();
		float dispersaoInicial = folhaDeSprites.pixels(0.25f);
		float densidade = Tela.getTela().getDensidade();

		for (int i = CONTAGEM_DE_FRAGMENTOS - 1; i >= 0; i--) {
			// Vamos posicionar cada fragmento, de modo que eles iniciem em posições aleatórias, mas
			// próximas entre si
			x[i] = xCentro + jogo.numeroAleatorio(-dispersaoInicial, dispersaoInicial);
			y[i] = yCentro + jogo.numeroAleatorio(-dispersaoInicial, dispersaoInicial);

			// Calcula o ângulo do voo do fragmento
			float anguloEmRadianos = jogo.numeroAleatorio(6.283185307179586476925286766559f);
			seno[i] = (float)Math.sin(anguloEmRadianos);
			cosseno[i] = (float)Math.cos(anguloEmRadianos);

			// A velocidade de cada fragmento também será diferente
			velocidade[i] = densidade * jogo.numeroAleatorio(VELOCIDADE_MINIMA, VELOCIDADE_MAXIMA);
		}

		setX(x);
		setY(y);
		setSeno(seno);
		setCosseno(cosseno);
		setVelocidade(velocidade);

		setCoordenadasDeModelo(folhaDeSprites.getCoordenadasDeModeloDoFragmentoDaExplosao());
		if (isExplosaoDaNave()) {
			setCoordenadasDeTextura(folhaDeSprites.getCoordenadasDeTexturaDoFragmentoDaExplosaoDaNave());
		} else {
			setCoordenadasDeTextura(folhaDeSprites.getCoordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo());
		}
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		setX(null);
		setY(null);
		setSeno(null);
		setCosseno(null);
		setVelocidade(null);
		setCoordenadasDeModelo(null);
		setCoordenadasDeTextura(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setFolhaDeSprites(null);
		setInterpolador(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe ElementoDeTela, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	@Override
	protected void processeUmQuadroSemPausa(float deltaSegundos) {
		float tempoDaAnimacao = getTempoDaAnimacao() + deltaSegundos;

		if (tempoDaAnimacao >= DURACAO_DA_ANIMACAO) {
			// Vamos definir o tempo da animação como o tempo final, pois ainda seremos desenhados
			// na tela mais uma última vez
			setTempoDaAnimacao(DURACAO_DA_ANIMACAO);

			// Não há mais nada o que processar, vamos apenas pedir para sermos removidos da lista
			// de elementos a qual nós pertencemos
			removaEDestrua();
			return;
		}

		setTempoDaAnimacao(tempoDaAnimacao);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void desenheUmQuadro() {
		Tela tela = Tela.getTela();
		Imagem imagem = getFolhaDeSprites().getImagem();
		float[] x = getX();
		float[] y = getY();
		float[] seno = getSeno();
		float[] cosseno = getCosseno();
		float[] velocidade = getVelocidade();

		// O quão próximo estamos do fim da animação
		float percentualAtual = (getInterpolador().interpole(getTempoDaAnimacao() / DURACAO_DA_ANIMACAO));

		float alpha = 1.0f - percentualAtual;

		CoordenadasDeModelo coordenadasDeModelo = getCoordenadasDeModelo();
		CoordenadasDeTextura coordenadasDeTextura = getCoordenadasDeTextura();

		for (int i = CONTAGEM_DE_FRAGMENTOS - 1; i >= 0; i--) {
			// Calcula a nova posição de cada fragmento
			float novoX = x[i] + (velocidade[i] * cosseno[i] * percentualAtual);
			float novoY = y[i] + (velocidade[i] * seno[i] * percentualAtual);
			tela.desenhe(imagem, coordenadasDeModelo, alpha, coordenadasDeTextura, novoX, novoY);
		}
	}
}
