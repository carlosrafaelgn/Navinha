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
import br.com.carlosrafaelgn.navinha.modelo.desenho.Imagem;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Ponto;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;

public final class CampoEstelar extends ElementoDeTelaComPausa {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	public static final int CONTAGEM_DE_ESTRELAS = 400;

	private static final float VELOCIDADE_LENTA = 10.0f;
	private static final float VELOCIDADE_RAPIDA = 30.0f;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private float tamanho, larguraNaImagem, alturaNaImagem;
	private float[] esquerda, cima, esquerdaImagem, cimaImagem;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public CampoEstelar(FolhaDeSprites folhaDeSprites) {
		setFolhaDeSprites(folhaDeSprites);

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

	private float getTamanho() {
		return tamanho;
	}

	private void setTamanho(float tamanho) {
		this.tamanho = tamanho;
	}

	private float getLarguraNaImagem() {
		return larguraNaImagem;
	}

	private void setLarguraNaImagem(float larguraNaImagem) {
		this.larguraNaImagem = larguraNaImagem;
	}

	private float getAlturaNaImagem() {
		return alturaNaImagem;
	}

	private void setAlturaNaImagem(float alturaNaImagem) {
		this.alturaNaImagem = alturaNaImagem;
	}

	private float[] getEsquerda() {
		return esquerda;
	}

	private void setEsquerda(float[] esquerda) {
		this.esquerda = esquerda;
	}

	private float[] getCima() {
		return cima;
	}

	private void setCima(float[] cima) {
		this.cima = cima;
	}

	private float[] getEsquerdaImagem() {
		return esquerdaImagem;
	}

	private void setEsquerdaImagem(float[] esquerdaImagem) {
		this.esquerdaImagem = esquerdaImagem;
	}

	private float[] getCimaImagem() {
		return cimaImagem;
	}

	private void setCimaImagem(float[] cimaImagem) {
		this.cimaImagem = cimaImagem;
	}

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getEsquerda() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void carregueInternamente() {
		Tela tela = Tela.getTela();
		Jogo jogo = Jogo.getJogo();

		// Cria a quantidade de estrelas pedidas, distribuídas uniformemente ao longo da tela

		float[] x = new float[CONTAGEM_DE_ESTRELAS];
		float[] y = new float[CONTAGEM_DE_ESTRELAS];
		float[] imagemX = new float[CONTAGEM_DE_ESTRELAS];
		float[] imagemY = new float[CONTAGEM_DE_ESTRELAS];

		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();
		Imagem imagem = folhaDeSprites.getImagem();
		int i, metade = CONTAGEM_DE_ESTRELAS / 2;
		Ponto ponto = new Ponto();

		// Define o tamanho de cada estrela como um valor inteiro, de pelo menos 1 pixel
		float tamanho = (folhaDeSprites.pixels(1.0f) / 24.0f);
		if (tamanho < 1.0f) {
			tamanho = 1.0f;
		}
		setTamanho(tamanho);

		float larguraDaVista = tela.getLarguraDaVista();
		float alturaDaVista = tela.getAlturaDaVista();

		// A metade das estrelas que ficará ao fundo será mais escura
		for (i = CONTAGEM_DE_ESTRELAS - 1; i >= metade; i--) {
			x[i] = jogo.numeroAleatorio(larguraDaVista);
			y[i] = jogo.numeroAleatorio(alturaDaVista);
			folhaDeSprites.escolhaUmPontoNaTexturaDoCampoEstelar(ponto, tamanho, false);
			imagemX[i] = ponto.getX();
			imagemY[i] = ponto.getY();
		}

		// A outra metade, que ficará à frente, será mais clara
		for ( ; i >= 0; i--) {
			x[i] = jogo.numeroAleatorio(larguraDaVista);
			y[i] = jogo.numeroAleatorio(alturaDaVista);
			folhaDeSprites.escolhaUmPontoNaTexturaDoCampoEstelar(ponto, tamanho, true);
			imagemX[i] = ponto.getX();
			imagemY[i] = ponto.getY();
		}

		setLarguraNaImagem(1.0f / imagem.getLargura());
		setAlturaNaImagem(1.0f / imagem.getAltura());
		setEsquerda(x);
		setCima(y);
		setEsquerdaImagem(imagemX);
		setCimaImagem(imagemY);
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		setEsquerda(null);
		setCima(null);
		setEsquerdaImagem(null);
		setCimaImagem(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setFolhaDeSprites(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe ElementoDeTela, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	@Override
	protected void processeUmQuadroSemPausa(float deltaSegundos) {
		float alturaDaVista = Tela.getTela().getAlturaDaVista();

		float tamanho = getTamanho();
		float velocidade;
		float[] y = getCima();

		int i, metade = CONTAGEM_DE_ESTRELAS / 2;

		// Uma metade, que ficará ao fundo, será mais lenta
		velocidade = VELOCIDADE_LENTA * tamanho;
		for (i = CONTAGEM_DE_ESTRELAS - 1; i >= metade; i--) {
			// Ao chegar na parte de baixo da tela, a estrela volta para o topo
			float novoY = y[i] + (velocidade * deltaSegundos);
			if (novoY >= alturaDaVista) {
				y[i] = -tamanho;
			} else {
				y[i] = novoY;
			}
		}

		// A outra metade, que ficará à frente, será mais rápida
		velocidade = VELOCIDADE_RAPIDA * tamanho;
		for ( ; i >= 0; i--) {
			// Ao chegar na parte de baixo da tela, a estrela volta para o topo
			float novoY = y[i] + (velocidade * deltaSegundos);
			if (novoY >= alturaDaVista) {
				y[i] = -tamanho;
			} else {
				y[i] = novoY;
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void desenheUmQuadro() {
		Tela tela = Tela.getTela();
		Imagem imagem = getFolhaDeSprites().getImagem();
		float tamanho = getTamanho();
		float larguraNaImagem = getLarguraNaImagem();
		float alturaNaImagem = getAlturaNaImagem();
		float[] esquerda = getEsquerda();
		float[] cima = getCima();
		float[] esquerdaImagem = getEsquerdaImagem();
		float[] cimaImagem = getCimaImagem();

		for (int i = CONTAGEM_DE_ESTRELAS - 1; i >= 0; i--) {
			tela.desenhe(imagem,
				esquerda[i],
				cima[i],
				esquerda[i] + tamanho,
				cima[i] + tamanho,
				1.0f,
				esquerdaImagem[i],
				cimaImagem[i],
				esquerdaImagem[i] + larguraNaImagem,
				cimaImagem[i] + alturaNaImagem);
		}
	}
}
