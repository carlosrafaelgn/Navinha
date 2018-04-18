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
package br.com.carlosrafaelgn.navinha.modelo.texto;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;

import br.com.carlosrafaelgn.navinha.modelo.desenho.Imagem;
import br.com.carlosrafaelgn.navinha.modelo.desenho.AlinhamentoDoPivo;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.elemento.ElementoDeTela;

public abstract class Texto extends ElementoDeTela {
	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	private static class Linha {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		// Armazena a largura como float para auxiliar o OpenGL
		private float x, largura;
		private Caractere[] caracteres;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public Linha(float x, Caractere[] caracteres) {
			setX(x);
			setCaracteres(caracteres);
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getLargura() {
			return largura;
		}

		public void setLargura(float largura) {
			this.largura = largura;
		}

		public Caractere[] getCaracteres() {
			return caracteres;
		}

		public void setCaracteres(Caractere[] caracteres) {
			this.caracteres = caracteres;

			// Calcula a largura da linha
			float largura = 0.0f;

			for (Caractere caractere : caracteres) {
				largura += caractere.getLargura();
			}

			setLargura(largura);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private Alfabeto alfabeto;
	// Armazena a largura e a altura como float para auxiliar o OpenGL
	private float largura, altura, pivoX, pivoY;
	private final float espacamentoEntreLinhas;
	private int alinhamentoDoPivo, alinhamentoHorizontalDasLinhas;
	private String texto;
	private Linha[] linhas;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Texto(Alfabeto alfabeto, String texto, float espacamentoEntreLinhas, int alinhamentoDoPivo) {
		this.espacamentoEntreLinhas = espacamentoEntreLinhas;

		setAlfabeto(alfabeto);
		setAlinhamentoDoPivo(alinhamentoDoPivo);
		setAlinhamentoHorizontalDasLinhas(AlinhamentoDoPivo.HORIZONTAL_ESQUERDO);
		setTexto(texto);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public final Alfabeto getAlfabeto() {
		return alfabeto;
	}

	private void setAlfabeto(Alfabeto alfabeto) {
		this.alfabeto = alfabeto;
	}

	public final float getLargura() {
		return largura;
	}

	private void setLargura(float largura) {
		this.largura = largura;
	}

	public final float getAltura() {
		return altura;
	}

	private void setAltura(float altura) {
		this.altura = altura;
	}

	public final float getPivoX() {
		return pivoX;
	}

	private void setPivoX(float pivoX) {
		this.pivoX = pivoX;
	}

	public final float getPivoY() {
		return pivoY;
	}

	private void setPivoY(float pivoY) {
		this.pivoY = pivoY;
	}

	public float getEspacamentoEntreLinhas() {
		return espacamentoEntreLinhas;
	}

	public final int getAlinhamentoDoPivo() {
		return alinhamentoDoPivo;
	}

	public final void setAlinhamentoDoPivo(int alinhamentoDoPivo) {
		this.alinhamentoDoPivo = alinhamentoDoPivo;

		setPivoX(AlinhamentoDoPivo.calculePivoXDoModelo(alinhamentoDoPivo, getLargura()));
		setPivoY(AlinhamentoDoPivo.calculePivoYDoModelo(alinhamentoDoPivo, getAltura()));
	}

	public final int getAlinhamentoHorizontalDasLinhas() {
		return alinhamentoHorizontalDasLinhas;
	}

	public final void setAlinhamentoHorizontalDasLinhas(int alinhamentoHorizontalDasLinhas) {
		this.alinhamentoHorizontalDasLinhas = alinhamentoHorizontalDasLinhas;

		Linha[] linhas = getLinhas();
		if (linhas == null) {
			return;
		}

		float largura = getLargura();

		switch (alinhamentoHorizontalDasLinhas) {
		case AlinhamentoDoPivo.HORIZONTAL_ESQUERDO:
			// Alinha todas as linhas à esquerda
			for (Linha linha : linhas) {
				linha.setX(0.0f);
			}
			return;

		case AlinhamentoDoPivo.HORIZONTAL_CENTRO:
			// Centraliza todas as linhas
			for (Linha linha : linhas) {
				linha.setX((largura - linha.getLargura()) * 0.5f);
			}
			return;

		case AlinhamentoDoPivo.HORIZONTAL_DIREITO:
			// Alinha todas as linhas à direita
			for (Linha linha : linhas) {
				linha.setX(largura - linha.getLargura());
			}
			return;
		}

		throw new IllegalArgumentException("alinhamentoHorizontalDasLinhas inválido");
	}

	public final String getTexto() {
		return texto;
	}

	public final void setTexto(String texto) {
		this.texto = texto;

		if (texto == null) {
			return;
		}

		// Mede cada um dos caracteres, e cria uma instância de Texto com os caracteres existentes
		// (se um caractere não existir em nosso alfabeto, ele simplesmente será ignorado)

		Alfabeto alfabeto = getAlfabeto();

		float larguraDaLinha;
		float larguraDaMaiorLinha = 0.0f;
		SparseArray<Caractere> caracteresDoAlfabeto = alfabeto.getCaracteres();
		ArrayList<Caractere> caracteresDaLinha = new ArrayList<>(texto.length());
		ArrayList<Linha> linhas = new ArrayList<>();

		for (int i = 0; i < texto.length(); i++) {
			char c = texto.charAt(i);

			if (c == '\n') {
				Linha linha = new Linha(0.0f, caracteresDaLinha.toArray(new Caractere[caracteresDaLinha.size()]));
				caracteresDaLinha.clear();

				larguraDaLinha = linha.getLargura();
				if (larguraDaMaiorLinha < larguraDaLinha) {
					larguraDaMaiorLinha = larguraDaLinha;
				}

				linhas.add(linha);

				continue;
			}

			Caractere caractere = caracteresDoAlfabeto.get(c);
			if (caractere != null) {
				caracteresDaLinha.add(caractere);
			}
		}

		// Verifica se é necessário criar a última linha
		if (!caracteresDaLinha.isEmpty()) {
			Linha linha = new Linha(0.0f, caracteresDaLinha.toArray(new Caractere[caracteresDaLinha.size()]));
			caracteresDaLinha.clear();

			larguraDaLinha = linha.getLargura();
			if (larguraDaMaiorLinha < larguraDaLinha) {
				larguraDaMaiorLinha = larguraDaLinha;
			}

			linhas.add(linha);
		}

		setLargura(larguraDaMaiorLinha);
		setAltura(((float)linhas.size() * alfabeto.getAlturaDaLinha()) + ((float)(linhas.size() - 1) * getEspacamentoEntreLinhas()));

		// Limpa as linhas existentes
		libere();

		// Armazena as novas linhas
		setLinhas(linhas.toArray(new Linha[linhas.size()]));

		// Redefine os alinhamentos para posicionar o pivô e as linhas corretamente
		setAlinhamentoDoPivo(getAlinhamentoDoPivo());
		setAlinhamentoHorizontalDasLinhas(getAlinhamentoHorizontalDasLinhas());
	}

	private Linha[] getLinhas() {
		return linhas;
	}

	private void setLinhas(Linha[] linhas) {
		this.linhas = linhas;
	}

	@Override
	public final boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getLinhas() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void carregueInternamente() {
		// Recria o texto
		setTexto(getTexto());
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		Arrays.fill(getLinhas(), null);
		setLinhas(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setAlfabeto(null);
		setTexto(null);
	}

	protected final void desenhe(float alpha, float destinoX, float destinoY) {
		Tela tela = Tela.getTela();

		Alfabeto alfabeto = getAlfabeto();
		Imagem imagem = alfabeto.getImagem();
		float alturaDaLinha = alfabeto.getAlturaDaLinha();
		float espacamentoEntreLinhas = getEspacamentoEntreLinhas();

		float esquerdaDoTexto = destinoX - getPivoX();
		float cima = destinoY - getPivoY();

		for (Linha linha : getLinhas()) {
			float esquerda = esquerdaDoTexto + linha.getX();
			float baixo = cima + alturaDaLinha;

			for (Caractere caractere : linha.getCaracteres()) {
				float direita = esquerda + caractere.getLargura();

				tela.desenhe(imagem, esquerda, cima, direita, baixo, alpha, caractere.getCoordenadasDeTextura());

				// Avança para o próximo caractere
				esquerda = direita;
			}

			// Avança para a próxima linha
			cima += alturaDaLinha + espacamentoEntreLinhas;
		}
	}

	protected final void desenhe(float alpha, float escalaX, float escalaY, float destinoX, float destinoY) {
		Tela tela = Tela.getTela();

		Alfabeto alfabeto = getAlfabeto();
		Imagem imagem = alfabeto.getImagem();
		// Aplica a escalaY à altura da linha para acelerar os cálculos abaixo
		float alturaDaLinha = alfabeto.getAlturaDaLinha() * escalaY;
		float espacamentoEntreLinhas = getEspacamentoEntreLinhas() * escalaY;

		float esquerdaDoTexto = destinoX - (getPivoX() * escalaX);
		float cima = destinoY - (getPivoY() * escalaY);

		for (Linha linha : getLinhas()) {
			float esquerda = esquerdaDoTexto + (linha.getX() * escalaX);
			float baixo = cima + alturaDaLinha;

			for (Caractere caractere : linha.getCaracteres()) {
				float direita = esquerda + (caractere.getLargura() * escalaX);

				tela.desenhe(imagem, esquerda, cima, direita, baixo, alpha, caractere.getCoordenadasDeTextura());

				// Avança para o próximo caractere
				esquerda = direita;
			}

			// Avança para a próxima linha
			cima += alturaDaLinha + espacamentoEntreLinhas;
		}
	}

	protected final void desenhe(float alpha, float anguloEmRadianos, float destinoX, float destinoY) {
		Tela tela = Tela.getTela();

		Alfabeto alfabeto = getAlfabeto();
		Imagem imagem = alfabeto.getImagem();
		float alturaDaLinha = alfabeto.getAlturaDaLinha();
		float espacamentoEntreLinhas = getEspacamentoEntreLinhas();

		float esquerdaDoTexto = -getPivoX();
		float cima = -getPivoY();

		for (Linha linha : getLinhas()) {
			float esquerda = esquerdaDoTexto + linha.getX();
			float baixo = cima + alturaDaLinha;

			for (Caractere caractere : linha.getCaracteres()) {
				float direita = esquerda + caractere.getLargura();

				tela.desenhe(imagem, esquerda, cima, direita, baixo, alpha, caractere.getCoordenadasDeTextura(), anguloEmRadianos, destinoX, destinoY);

				// Avança para o próximo caractere
				esquerda = direita;
			}

			// Avança para a próxima linha
			cima += alturaDaLinha + espacamentoEntreLinhas;
		}
	}

	protected final void desenhe(float alpha, float escalaX, float escalaY, float anguloEmRadianos, float destinoX, float destinoY) {
		Tela tela = Tela.getTela();

		Alfabeto alfabeto = getAlfabeto();
		Imagem imagem = alfabeto.getImagem();
		// Aplica a escalaY à altura da linha para acelerar os cálculos abaixo
		float alturaDaLinha = alfabeto.getAlturaDaLinha() * escalaY;
		float espacamentoEntreLinhas = getEspacamentoEntreLinhas() * escalaY;

		float esquerdaDoTexto = -getPivoX() * escalaX;
		float cima = -getPivoY() * escalaY;

		for (Linha linha : getLinhas()) {
			float esquerda = esquerdaDoTexto + (linha.getX() * escalaX);
			float baixo = cima + alturaDaLinha;

			for (Caractere caractere : linha.getCaracteres()) {
				float direita = esquerda + (caractere.getLargura() * escalaX);

				tela.desenhe(imagem, esquerda, cima, direita, baixo, alpha, caractere.getCoordenadasDeTextura(), anguloEmRadianos, destinoX, destinoY);

				// Avança para o próximo caractere
				esquerda = direita;
			}

			// Avança para a próxima linha
			cima += alturaDaLinha + espacamentoEntreLinhas;
		}
	}
}
