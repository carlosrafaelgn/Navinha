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
package br.com.carlosrafaelgn.navinha.jogo.desenho;

import java.util.Arrays;

import br.com.carlosrafaelgn.navinha.R;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.Vetor;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeModelo;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeTextura;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Imagem;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Ponto;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public final class FolhaDeSprites extends Recurso {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final float ESPACAMENTO = 2.0f;

	private static final float LARGURA_NAVE_PARADA = 24.0f;
	private static final float LARGURA_NAVE_MOVENDO = 14.0f;
	private static final float ALTURA_NAVE = 24.0f;

	private static final float LARGURA_INIMIGO = 24.0f;
	private static final float ALTURA_INIMIGO = 24.0f;

	private static final float LARGURA_TIRO_NAVE = 4.0f;
	private static final float ALTURA_TIRO_NAVE = 12.0f;

	private static final float LARGURA_TIRO_INIMIGO = 4.0f;
	private static final float ALTURA_TIRO_INIMIGO = 8.0f;

	private static final float TEXTURA_TAMANHO_BASE = 40.0f;

	private static final float TEXTURA_ESQUERDA_TIRO_NAVE = 10.0f;
	private static final float TEXTURA_CIMA_TIRO_NAVE = 90.0f;
	private static final float TEXTURA_LARGURA_TIRO_NAVE = 20.0f;
	private static final float TEXTURA_ALTURA_TIRO_NAVE = 28.0f;

	private static final float TEXTURA_ESQUERDA_TIRO_INIMIGO = 52.0f;
	private static final float TEXTURA_CIMA_TIRO_INIMIGO = 92.0f;
	private static final float TEXTURA_LARGURA_TIRO_INIMIGO = 20.0f;
	private static final float TEXTURA_ALTURA_TIRO_INIMIGO = 24.0f;

	private static final float TEXTURA_ESQUERDA_FRAGMENTO_EXPLOSAO_NAVE = 96.0f;
	private static final float TEXTURA_CIMA_FRAGMENTO_EXPLOSAO_NAVE = 54.0f;
	private static final float TEXTURA_ESQUERDA_FRAGMENTO_EXPLOSAO_INIMIGO = 96.0f;
	private static final float TEXTURA_CIMA_FRAGMENTO_EXPLOSAO_INIMIGO = 96.0f;
	private static final float TEXTURA_TAMANHO_FRAGMENTO_EXPLOSAO = 14.0f;

	private static final float TEXTURA_ESQUERDA_FADE = 54.0f;
	private static final float TEXTURA_CIMA_FADE = 54.0f;

	private static final float TEXTURA_CAMPO_ESTELAR_ESCURO_CIMA = 42.0f;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private Imagem imagem;

	private float larguraDaNaveParada, larguraDaNaveMovendo, alturaDaNave;
	private float larguraDoInimigo, alturaDoInimigo;
	private float larguraDoTiroDaNave, alturaDoTiroDaNave;
	private float larguraDoTiroDoInimigo, alturaDoTiroDoInimigo;

	private CoordenadasDeModelo coordenadasDeModeloDaNave;
	private CoordenadasDeModelo[] coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento;
	private CoordenadasDeModelo coordenadasDeModeloDoInimigo;
	private CoordenadasDeModelo coordenadasDeModeloDosLimitesDoInimigo;
	private CoordenadasDeModelo coordenadasDeModeloDoTiroDaNave;
	private CoordenadasDeModelo coordenadasDeModeloDosLimitesDoTiroDaNave;
	private CoordenadasDeModelo coordenadasDeModeloDoTiroDoInimigo;
	private CoordenadasDeModelo coordenadasDeModeloDosLimitesDoTiroDoInimigo;
	private CoordenadasDeModelo coordenadasDeModeloDoFragmentoDaExplosao;

	private CoordenadasDeTextura[] coordenadasDeTexturaDaNavePorTipoDeMovimento;
	private Vetor<CoordenadasDeTextura>[] coordenadasDeTexturaDoInimigoPorVida;
	private CoordenadasDeTextura coordenadasDeTexturaDoTiroDaNave;
	private CoordenadasDeTextura coordenadasDeTexturaDoTiroDoInimigo;
	private CoordenadasDeTextura coordenadasDeTexturaDoFragmentoDaExplosaoDaNave;
	private CoordenadasDeTextura coordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo;
	private CoordenadasDeTextura coordenadasDeTexturaDoFade;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public FolhaDeSprites() {
		carregueInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public Imagem getImagem() {
		return imagem;
	}

	private void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	public float getLarguraDaNaveParada() {
		return larguraDaNaveParada;
	}

	private void setLarguraDaNaveParada(float larguraDaNaveParada) {
		this.larguraDaNaveParada = larguraDaNaveParada;
	}

	public float getLarguraDaNaveMovendo() {
		return larguraDaNaveMovendo;
	}

	private void setLarguraDaNaveMovendo(float larguraDaNaveMovendo) {
		this.larguraDaNaveMovendo = larguraDaNaveMovendo;
	}

	public float getAlturaDaNave() {
		return alturaDaNave;
	}

	private void setAlturaDaNave(float alturaDaNave) {
		this.alturaDaNave = alturaDaNave;
	}

	public float getLarguraDoInimigo() {
		return larguraDoInimigo;
	}

	private void setLarguraDoInimigo(float larguraDoInimigo) {
		this.larguraDoInimigo = larguraDoInimigo;
	}

	public float getAlturaDoInimigo() {
		return alturaDoInimigo;
	}

	private void setAlturaDoInimigo(float alturaDoInimigo) {
		this.alturaDoInimigo = alturaDoInimigo;
	}

	public float getLarguraDoTiroDaNave() {
		return larguraDoTiroDaNave;
	}

	private void setLarguraDoTiroDaNave(float larguraDoTiroDaNave) {
		this.larguraDoTiroDaNave = larguraDoTiroDaNave;
	}

	public float getAlturaDoTiroDaNave() {
		return alturaDoTiroDaNave;
	}

	private void setAlturaDoTiroDaNave(float alturaDoTiroDaNave) {
		this.alturaDoTiroDaNave = alturaDoTiroDaNave;
	}

	public float getLarguraDoTiroDoInimigo() {
		return larguraDoTiroDoInimigo;
	}

	private void setLarguraDoTiroDoInimigo(float larguraDoTiroDoInimigo) {
		this.larguraDoTiroDoInimigo = larguraDoTiroDoInimigo;
	}

	public float getAlturaDoTiroDoInimigo() {
		return alturaDoTiroDoInimigo;
	}

	private void setAlturaDoTiroDoInimigo(float alturaDoTiroDoInimigo) {
		this.alturaDoTiroDoInimigo = alturaDoTiroDoInimigo;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDaNave() {
		return coordenadasDeModeloDaNave;
	}

	private void setCoordenadasDeModeloDaNave(CoordenadasDeModelo coordenadasDeModeloDaNave) {
		this.coordenadasDeModeloDaNave = coordenadasDeModeloDaNave;
	}

	private CoordenadasDeModelo[] getCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento() {
		return coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento;
	}

	private void setCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento(CoordenadasDeModelo[] coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento) {
		this.coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento = coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento(int tipoDeMovimento) {
		return coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento[tipoDeMovimento];
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDoInimigo() {
		return coordenadasDeModeloDoInimigo;
	}

	private void setCoordenadasDeModeloDoInimigo(CoordenadasDeModelo coordenadasDeModeloDoInimigo) {
		this.coordenadasDeModeloDoInimigo = coordenadasDeModeloDoInimigo;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDosLimitesDoInimigo() {
		return coordenadasDeModeloDosLimitesDoInimigo;
	}

	private void setCoordenadasDeModeloDosLimitesDoInimigo(CoordenadasDeModelo coordenadasDeModeloDosLimitesDoInimigo) {
		this.coordenadasDeModeloDosLimitesDoInimigo = coordenadasDeModeloDosLimitesDoInimigo;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDoTiroDaNave() {
		return coordenadasDeModeloDoTiroDaNave;
	}

	private void setCoordenadasDeModeloDoTiroDaNave(CoordenadasDeModelo coordenadasDeModeloDoTiroDaNave) {
		this.coordenadasDeModeloDoTiroDaNave = coordenadasDeModeloDoTiroDaNave;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDosLimitesDoTiroDaNave() {
		return coordenadasDeModeloDosLimitesDoTiroDaNave;
	}

	private void setCoordenadasDeModeloDosLimitesDoTiroDaNave(CoordenadasDeModelo coordenadasDeModeloDosLimitesDoTiroDaNave) {
		this.coordenadasDeModeloDosLimitesDoTiroDaNave = coordenadasDeModeloDosLimitesDoTiroDaNave;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDoTiroDoInimigo() {
		return coordenadasDeModeloDoTiroDoInimigo;
	}

	private void setCoordenadasDeModeloDoTiroDoInimigo(CoordenadasDeModelo coordenadasDeModeloDoTiroDoInimigo) {
		this.coordenadasDeModeloDoTiroDoInimigo = coordenadasDeModeloDoTiroDoInimigo;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDosLimitesDoTiroDoInimigo() {
		return coordenadasDeModeloDosLimitesDoTiroDoInimigo;
	}

	private void setCoordenadasDeModeloDosLimitesDoTiroDoInimigo(CoordenadasDeModelo coordenadasDeModeloDosLimitesDoTiroDoInimigo) {
		this.coordenadasDeModeloDosLimitesDoTiroDoInimigo = coordenadasDeModeloDosLimitesDoTiroDoInimigo;
	}

	public CoordenadasDeModelo getCoordenadasDeModeloDoFragmentoDaExplosao() {
		return coordenadasDeModeloDoFragmentoDaExplosao;
	}

	private void setCoordenadasDeModeloDoFragmentoDaExplosao(CoordenadasDeModelo coordenadasDeModeloDoFragmentoDaExplosao) {
		this.coordenadasDeModeloDoFragmentoDaExplosao = coordenadasDeModeloDoFragmentoDaExplosao;
	}

	private CoordenadasDeTextura[] getCoordenadasDeTexturaDaNavePorTipoDeMovimento() {
		return coordenadasDeTexturaDaNavePorTipoDeMovimento;
	}

	private void setCoordenadasDeTexturaDaNavePorTipoDeMovimento(CoordenadasDeTextura[] coordenadasDeTexturaDaNavePorTipoDeMovimento) {
		this.coordenadasDeTexturaDaNavePorTipoDeMovimento = coordenadasDeTexturaDaNavePorTipoDeMovimento;
	}

	public CoordenadasDeTextura getCoordenadasDeTexturaDaNavePorTipoDeMovimento(int tipoDeMovimento) {
		return coordenadasDeTexturaDaNavePorTipoDeMovimento[tipoDeMovimento];
	}

	private Vetor<CoordenadasDeTextura>[] getCoordenadasDeTexturaDoInimigoPorVida() {
		return coordenadasDeTexturaDoInimigoPorVida;
	}

	private void setCoordenadasDeTexturaDoInimigoPorVida(Vetor<CoordenadasDeTextura>[] coordenadasDeTexturaDoInimigoPorVida) {
		this.coordenadasDeTexturaDoInimigoPorVida = coordenadasDeTexturaDoInimigoPorVida;
	}

	public Vetor<CoordenadasDeTextura> getCoordenadasDeTexturaDoInimigoPorVida(int vidas) {
		return coordenadasDeTexturaDoInimigoPorVida[vidas - 1];
	}

	public CoordenadasDeTextura getCoordenadasDeTexturaDoTiroDaNave() {
		return coordenadasDeTexturaDoTiroDaNave;
	}

	private void setCoordenadasDeTexturaDoTiroDaNave(CoordenadasDeTextura coordenadasDeTexturaDoTiroDaNave) {
		this.coordenadasDeTexturaDoTiroDaNave = coordenadasDeTexturaDoTiroDaNave;
	}

	public CoordenadasDeTextura getCoordenadasDeTexturaDoTiroDoInimigo() {
		return coordenadasDeTexturaDoTiroDoInimigo;
	}

	private void setCoordenadasDeTexturaDoTiroDoInimigo(CoordenadasDeTextura coordenadasDeTexturaDoTiroDoInimigo) {
		this.coordenadasDeTexturaDoTiroDoInimigo = coordenadasDeTexturaDoTiroDoInimigo;
	}

	public CoordenadasDeTextura getCoordenadasDeTexturaDoFragmentoDaExplosaoDaNave() {
		return coordenadasDeTexturaDoFragmentoDaExplosaoDaNave;
	}

	private void setCoordenadasDeTexturaDoFragmentoDaExplosaoDaNave(CoordenadasDeTextura coordenadasDeTexturaDoFragmentoDaExplosaoDaNave) {
		this.coordenadasDeTexturaDoFragmentoDaExplosaoDaNave = coordenadasDeTexturaDoFragmentoDaExplosaoDaNave;
	}

	public CoordenadasDeTextura getCoordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo() {
		return coordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo;
	}

	private void setCoordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo(CoordenadasDeTextura coordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo) {
		this.coordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo = coordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo;
	}

	public CoordenadasDeTextura getCoordenadasDeTexturaDoFade() {
		return coordenadasDeTexturaDoFade;
	}

	private void setCoordenadasDeTexturaDoFade(CoordenadasDeTextura coordenadasDeTexturaDoFade) {
		this.coordenadasDeTexturaDoFade = coordenadasDeTexturaDoFade;
	}

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getImagem() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private CoordenadasDeTextura crieCoordenadasDeTexturaBase(int indiceX, int indiceY) {
		float densidade = Tela.getTela().getDensidade();
		float esquerda = (TEXTURA_TAMANHO_BASE + ESPACAMENTO) * densidade * (float)indiceX;
		float cima = (TEXTURA_TAMANHO_BASE + ESPACAMENTO) * densidade * (float)indiceY;
		float tamanho = TEXTURA_TAMANHO_BASE * densidade;
		Imagem imagem = getImagem();
		return new CoordenadasDeTextura(imagem.getLargura(), imagem.getAltura(), esquerda, cima, esquerda + tamanho, cima + tamanho);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void carregueInternamente() {
		Imagem imagem = new Imagem(R.drawable.sprites);
		setImagem(imagem);

		float densidade = Tela.getTela().getDensidade();
		float esquerda, cima, largura, larguraMovendo, altura;

		// As dimensões calculadas aqui, se referem à área limite (que causam colisão) de cada
		// elemento, e não a seus modelos ou texturas
		setLarguraDaNaveParada((float)((int)(LARGURA_NAVE_PARADA * densidade)));
		setLarguraDaNaveMovendo((float)((int)(LARGURA_NAVE_MOVENDO * densidade)));
		setAlturaDaNave((float)((int)(ALTURA_NAVE * densidade)));
		setLarguraDoInimigo((float)((int)(LARGURA_INIMIGO * densidade)));
		setAlturaDoInimigo((float)((int)(ALTURA_INIMIGO * densidade)));
		setLarguraDoTiroDaNave((float)((int)(LARGURA_TIRO_NAVE * densidade)));
		setAlturaDoTiroDaNave((float)((int)(ALTURA_TIRO_NAVE * densidade)));
		setLarguraDoTiroDoInimigo((float)((int)(LARGURA_TIRO_INIMIGO * densidade)));
		setAlturaDoTiroDoInimigo((float)((int)(ALTURA_TIRO_INIMIGO * densidade)));

		// Nave
		largura = TEXTURA_TAMANHO_BASE * densidade;
		altura = largura;
		setCoordenadasDeModeloDaNave(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));
		largura = getLarguraDaNaveParada();
		larguraMovendo = getLarguraDaNaveMovendo();
		altura = getAlturaDaNave();
		setCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento(new CoordenadasDeModelo[]{
			new CoordenadasDeModelo(
				0.5f * largura,
				0.5f * altura,
				largura,
				altura
			),
			new CoordenadasDeModelo(
				0.5f * larguraMovendo,
				0.5f * altura,
				larguraMovendo,
				altura
			),
			new CoordenadasDeModelo(
				0.5f * larguraMovendo,
				0.5f * altura,
				larguraMovendo,
				altura
			)
		});
		setCoordenadasDeTexturaDaNavePorTipoDeMovimento(new CoordenadasDeTextura[]{
			crieCoordenadasDeTexturaBase(0, 0), // Parado
			crieCoordenadasDeTexturaBase(1, 0), // Indo para a esquerda
			crieCoordenadasDeTexturaBase(2, 0)  // Indo para a direita
		});

		// Inimigo
		largura = TEXTURA_TAMANHO_BASE * densidade;
		altura = largura;
		setCoordenadasDeModeloDoInimigo(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));
		largura = getLarguraDoInimigo();
		altura = getAlturaDoInimigo();
		setCoordenadasDeModeloDosLimitesDoInimigo(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));
		setCoordenadasDeTexturaDoInimigoPorVida(new Vetor[]{
			// Vermelho
			new Vetor<>(
				crieCoordenadasDeTexturaBase(3, 0),
				crieCoordenadasDeTexturaBase(4, 0),
				crieCoordenadasDeTexturaBase(5, 0),
				crieCoordenadasDeTexturaBase(6, 0),
				crieCoordenadasDeTexturaBase(7, 0),
				crieCoordenadasDeTexturaBase(8, 0)
			),
			// Amarelo
			new Vetor<>(
				crieCoordenadasDeTexturaBase(3, 1),
				crieCoordenadasDeTexturaBase(4, 1),
				crieCoordenadasDeTexturaBase(5, 1),
				crieCoordenadasDeTexturaBase(6, 1),
				crieCoordenadasDeTexturaBase(7, 1),
				crieCoordenadasDeTexturaBase(8, 1)
			),
			// Verde
			new Vetor<>(
				crieCoordenadasDeTexturaBase(3, 2),
				crieCoordenadasDeTexturaBase(4, 2),
				crieCoordenadasDeTexturaBase(5, 2),
				crieCoordenadasDeTexturaBase(6, 2),
				crieCoordenadasDeTexturaBase(7, 2),
				crieCoordenadasDeTexturaBase(8, 2)
			)
		});

		// Tiro da nave
		esquerda = TEXTURA_ESQUERDA_TIRO_NAVE * densidade;
		cima = TEXTURA_CIMA_TIRO_NAVE * densidade;
		largura = TEXTURA_LARGURA_TIRO_NAVE * densidade;
		altura = TEXTURA_ALTURA_TIRO_NAVE * densidade;
		setCoordenadasDeModeloDoTiroDaNave(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));
		setCoordenadasDeTexturaDoTiroDaNave(new CoordenadasDeTextura(
			imagem,
			esquerda,
			cima,
			esquerda + largura,
			cima + altura
		));
		largura = getLarguraDoTiroDaNave();
		altura = getAlturaDoTiroDaNave();
		setCoordenadasDeModeloDosLimitesDoTiroDaNave(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));

		// Tiro do inimigo
		esquerda = TEXTURA_ESQUERDA_TIRO_INIMIGO * densidade;
		cima = TEXTURA_CIMA_TIRO_INIMIGO * densidade;
		largura = TEXTURA_LARGURA_TIRO_INIMIGO * densidade;
		altura = TEXTURA_ALTURA_TIRO_INIMIGO * densidade;
		setCoordenadasDeModeloDoTiroDoInimigo(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));
		setCoordenadasDeTexturaDoTiroDoInimigo(new CoordenadasDeTextura(
			imagem,
			esquerda,
			cima,
			esquerda + largura,
			cima + altura
		));
		largura = getLarguraDoTiroDoInimigo();
		altura = getAlturaDoTiroDoInimigo();
		setCoordenadasDeModeloDosLimitesDoTiroDoInimigo(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));

		// Fragmentos da explosão da nave e do inimigo (também são quadrados)
		esquerda = TEXTURA_ESQUERDA_FRAGMENTO_EXPLOSAO_NAVE * densidade;
		cima = TEXTURA_CIMA_FRAGMENTO_EXPLOSAO_NAVE * densidade;
		largura = TEXTURA_TAMANHO_FRAGMENTO_EXPLOSAO * densidade;
		altura = largura;
		setCoordenadasDeModeloDoFragmentoDaExplosao(new CoordenadasDeModelo(
			0.5f * largura,
			0.5f * altura,
			largura,
			altura
		));
		setCoordenadasDeTexturaDoFragmentoDaExplosaoDaNave(new CoordenadasDeTextura(
			imagem,
			esquerda,
			cima,
			esquerda + largura,
			cima + altura
		));

		esquerda = TEXTURA_ESQUERDA_FRAGMENTO_EXPLOSAO_INIMIGO * densidade;
		cima = TEXTURA_CIMA_FRAGMENTO_EXPLOSAO_INIMIGO * densidade;
		setCoordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo(new CoordenadasDeTextura(
			imagem,
			esquerda,
			cima,
			esquerda + largura,
			cima + altura
		));

		// Fade (o tamanho do fade não importa, pois ele será esticado para cobrir a tela inteira)
		esquerda = TEXTURA_ESQUERDA_FADE * densidade;
		cima = TEXTURA_CIMA_FADE * densidade;
		largura = 8.0f * densidade;
		altura = largura;
		setCoordenadasDeTexturaDoFade(new CoordenadasDeTextura(
			imagem,
			esquerda,
			cima,
			esquerda + largura,
			cima + altura
		));
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		destruaComSeguranca(getImagem());
		setImagem(null);

		setCoordenadasDeModeloDaNave(null);

		CoordenadasDeModelo[] coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento = getCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento();
		if (coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento != null) {
			Arrays.fill(coordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento, null);
			setCoordenadasDeModeloDosLimitesDaNavePorTipoDeMovimento(null);
		}

		setCoordenadasDeModeloDoInimigo(null);
		setCoordenadasDeModeloDosLimitesDoInimigo(null);
		setCoordenadasDeModeloDoTiroDaNave(null);
		setCoordenadasDeModeloDosLimitesDoTiroDaNave(null);
		setCoordenadasDeModeloDoTiroDoInimigo(null);
		setCoordenadasDeModeloDosLimitesDoTiroDoInimigo(null);
		setCoordenadasDeModeloDoFragmentoDaExplosao(null);

		CoordenadasDeTextura[] coordenadasDeTexturaDaNavePorTipoDeMovimento = getCoordenadasDeTexturaDaNavePorTipoDeMovimento();
		if (coordenadasDeTexturaDaNavePorTipoDeMovimento != null) {
			Arrays.fill(coordenadasDeTexturaDaNavePorTipoDeMovimento, null);
			setCoordenadasDeTexturaDaNavePorTipoDeMovimento(null);
		}

		Vetor<CoordenadasDeTextura>[] coordenadasDeTexturaDoInimigoPorVida = getCoordenadasDeTexturaDoInimigoPorVida();
		if (coordenadasDeTexturaDoInimigoPorVida != null) {
			Arrays.fill(coordenadasDeTexturaDoInimigoPorVida, null);
			setCoordenadasDeTexturaDoInimigoPorVida(null);
		}

		setCoordenadasDeTexturaDoTiroDaNave(null);
		setCoordenadasDeTexturaDoTiroDoInimigo(null);
		setCoordenadasDeTexturaDoFragmentoDaExplosaoDaNave(null);
		setCoordenadasDeTexturaDoFragmentoDaExplosaoDoInimigo(null);
		setCoordenadasDeTexturaDoFade(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		// Nesse caso não há nada a fazer :)
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public float unidades(float pixels) {
		// Nossa unidade básica de medida a altura de uma nave
		return pixels / getAlturaDaNave();
	}

	public float pixels(float unidades) {
		// Nossa unidade básica de medida a altura de uma nave
		return unidades * getAlturaDaNave();
	}

	public void escolhaUmPontoNaTexturaDoCampoEstelar(Ponto ponto, float tamanhoDaEstrela, boolean claro) {
		Jogo jogo = Jogo.getJogo();

		Imagem imagem = getImagem();

		float densidade = Tela.getTela().getDensidade();
		float cima = TEXTURA_CAMPO_ESTELAR_ESCURO_CIMA * densidade;
		float tamanhoBase = TEXTURA_TAMANHO_BASE * densidade;

		// Escolhe algum dos pixels do campo estelar, preenchendo o ponto com a posição sorteada
		ponto.setX(jogo.numeroAleatorio(tamanhoBase - tamanhoDaEstrela) / imagem.getLargura());

		float y = cima + jogo.numeroAleatorio((0.5f * tamanhoBase) - tamanhoDaEstrela);
		if (claro) {
			ponto.setY((y + (0.5f * tamanhoBase)) / imagem.getAltura());
		} else {
			ponto.setY(y / imagem.getAltura());
		}
	}
}
