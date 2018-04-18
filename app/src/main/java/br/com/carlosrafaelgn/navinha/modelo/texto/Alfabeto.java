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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.SparseArray;
import android.util.SparseIntArray;

import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeTextura;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Imagem;
import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public final class Alfabeto extends Recurso {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private Typeface fonte;
	// Armazena a altura da linha como float para auxiliar o OpenGL
	private float tamanhoDoTexto, alturaDaLinha;
	private int cor;
	private boolean suavizado;
	private char[] caracteresDisponiveis;
	private Imagem imagem;
	private SparseArray<Caractere> caracteres;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Alfabeto(Typeface fonte, float tamanhoDoTexto, int cor, boolean suavizado) {
		// Uma versão com alguns caracteres básicos
		this(fonte, tamanhoDoTexto, cor, suavizado, new char[]{
			' ', '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
			'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_',
			'`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', 'À', 'Á',
			'Â', 'Ã', 'Ç', 'É', 'Ê', 'Í', 'Ó', 'Ô', 'Õ', 'Ú', 'à', 'á', 'â', 'ã', 'ç', 'é',
			'ê', 'í', 'ó', 'ô', 'õ', 'ú'
		});
	}

	public Alfabeto(Typeface fonte, float tamanhoDoTexto, int cor, boolean suavizado, String caracteresDisponiveis) {
		this(fonte, tamanhoDoTexto, cor, suavizado, separeCaracteresDistintos(new String[] { caracteresDisponiveis }));
	}

	public Alfabeto(Typeface fonte, float tamanhoDoTexto, int cor, boolean suavizado, String... caracteresDisponiveis) {
		this(fonte, tamanhoDoTexto, cor, suavizado, separeCaracteresDistintos(caracteresDisponiveis));
	}

	private Alfabeto(Typeface fonte, float tamanhoDoTexto, int cor, boolean suavizado, char[] caracteresDisponiveis) {
		setFonte(fonte);
		setTamanhoDoTexto(tamanhoDoTexto);
		setCor(cor);
		setSuavizado(suavizado);
		setCaracteresDisponiveis(caracteresDisponiveis);

		carregueInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public Typeface getFonte() {
		return fonte;
	}

	private void setFonte(Typeface fonte) {
		this.fonte = fonte;
	}

	public float getTamanhoDoTexto() {
		return tamanhoDoTexto;
	}

	private void setTamanhoDoTexto(float tamanhoDoTexto) {
		this.tamanhoDoTexto = tamanhoDoTexto;
	}

	public float getAlturaDaLinha() {
		return alturaDaLinha;
	}

	private void setAlturaDaLinha(float alturaDaLinha) {
		this.alturaDaLinha = alturaDaLinha;
	}

	public int getCor() {
		return cor;
	}

	private void setCor(int cor) {
		this.cor = cor;
	}

	public boolean isSuavizado() {
		return suavizado;
	}

	private void setSuavizado(boolean suavizado) {
		this.suavizado = suavizado;
	}

	private char[] getCaracteresDisponiveis() {
		return caracteresDisponiveis;
	}

	private void setCaracteresDisponiveis(char[] caracteresDisponiveis) {
		this.caracteresDisponiveis = caracteresDisponiveis;
	}

	Imagem getImagem() {
		return imagem;
	}

	private void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	SparseArray<Caractere> getCaracteres() {
		return caracteres;
	}

	private void setCaracteres(SparseArray<Caractere> caracteres) {
		this.caracteres = caracteres;
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

	private static char[] separeCaracteresDistintos(String[] caracteres) {
		// Ao utilizar um SparseIntArray, economizamos memória, se comparado a um HashSet, além de
		// também garantimos que não teremos caracteres repetidos

		SparseIntArray caracteresFiltrados = new SparseIntArray(100);

		for (String string : caracteres) {
			for (int i = string.length() - 1; i >= 0; i--) {
				char c = string.charAt(i);
				if (c <= 0x1f || (c >= 0x7f && c <= 0x9f)) {
					// Ignora os caracteres de comando/controle ASCII por completo
					continue;
				}
				// O valor pouco importa, o que interessa mesmo é a chave
				caracteresFiltrados.put(string.charAt(i), 0);
			}
		}

		char[] caracteresDisponiveis = new char[caracteresFiltrados.size()];

		for (int i = caracteresFiltrados.size() - 1; i >= 0; i--) {
			caracteresDisponiveis[i] = (char)caracteresFiltrados.keyAt(i);
		}

		return caracteresDisponiveis;
	}

	@Override
	protected void carregueInternamente() {
		// Vamos obter todas as informações sobre essa fonte
		TextPaint paintTexto = new TextPaint();
		paintTexto.setAntiAlias(isSuavizado());
		paintTexto.setDither(false);
		paintTexto.setStyle(Paint.Style.FILL);
		paintTexto.setTextAlign(Paint.Align.LEFT);
		paintTexto.setTypeface(getFonte());
		paintTexto.setTextSize(getTamanhoDoTexto());
		paintTexto.setColor(getCor());

		char[] caracteresDisponiveis = getCaracteresDisponiveis();

		// Vamos tentar distribuir da forma mais "quadrada" possível
		int quantidadeDeColunas = (int)Math.sqrt(caracteresDisponiveis.length);
		int quantidadeDeLinhas = 0;

		int larguraDessaLinha = 0, larguraMaximaDeLinha = 0, caracteresNessaLinha = 0;
		int[] larguraDosCaracteres = new int[caracteresDisponiveis.length];

		for (int i = 0; i < caracteresDisponiveis.length; i++) {
			larguraDosCaracteres[i] = (int)Math.ceil(paintTexto.measureText(caracteresDisponiveis, i, 1));

			// O + 1 serve para adicionar um pixel extra entre cada coluna
			larguraDessaLinha += larguraDosCaracteres[i] + 1;

			caracteresNessaLinha++;

			if (caracteresNessaLinha >= quantidadeDeColunas) {
				// Remove o pixel extra da última coluna, afinal, queremos um pixel extra apenas
				// "entre" as colunas
				larguraDessaLinha--;

				// Só nos interessa a maior linha
				if (larguraMaximaDeLinha < larguraDessaLinha) {
					larguraMaximaDeLinha = larguraDessaLinha;
				}

				// Reinicia tudo para começar uma nova linha
				larguraDessaLinha = 0;
				caracteresNessaLinha = 0;

				quantidadeDeLinhas++;
			}
		}

		if (caracteresNessaLinha != 0) {
			// Não podemos nos esquecer de contabilizar os caracteres da última linha
			quantidadeDeLinhas++;
			if (larguraMaximaDeLinha < larguraDessaLinha) {
				larguraMaximaDeLinha = larguraDessaLinha;
			}
		}

		TextPaint.FontMetrics metricasDoTexto = paintTexto.getFontMetrics();

		// Arredonda para cima a altura da linha (se for 10.0, continuará 10, mas se for 10.1, ou
		// 10.01, virará 11)
		float alturaDeUmaLinha = (float)Math.ceil(metricasDoTexto.bottom - metricasDoTexto.top);

		float larguraTotal = larguraMaximaDeLinha;
		// Vamos adicionar um pixel extra entre cada uma das linhas
		float alturaTotal = (alturaDeUmaLinha * quantidadeDeLinhas) + (quantidadeDeLinhas - 1);

		// Cria um bitmap onde o texto será desenhado
		Bitmap bitmap = Bitmap.createBitmap(larguraMaximaDeLinha, (int)alturaTotal, Bitmap.Config.ARGB_8888);

		// Cria um Canvas com base nesse bitmap, de modo que tudo que fizermos ao Canvas, ficará
		// armazenado dentro do bitmap
		Canvas canvas = new Canvas(bitmap);

		SparseArray<Caractere> caracteres = new SparseArray<>(caracteresDisponiveis.length);

		// Repete o processo inteiro, só que dessa vez, desenhando
		caracteresNessaLinha = 0;

		// Posições x e y de onde desenhar o texto em uma linha
		float xDoTextoNaLinhaAtual = 0.0f;
		float yDentroDaLinhaDeTexto = (float)Math.ceil(-metricasDoTexto.top);
		float yDaLinhaAtual = 0.0f;

		for (int i = 0; i < caracteresDisponiveis.length; i++) {
			float larguraDesseCaractere = (float)larguraDosCaracteres[i];

			// Cria o objeto que armazenará as informações do caractere
			Caractere caractere = new Caractere(larguraDesseCaractere,
				new CoordenadasDeTextura(larguraTotal,
					alturaTotal,
					xDoTextoNaLinhaAtual,
					yDaLinhaAtual,
					xDoTextoNaLinhaAtual + larguraDesseCaractere,
					yDaLinhaAtual + alturaDeUmaLinha));

			// Apesar de gastar um pouco mais de processamento, precisamos garantir que não haja
			// caracteres repetidos
			int caractereAtual = (int)caracteresDisponiveis[i];
			if (caracteres.indexOfKey(caractereAtual) >= 0) {
				throw new RuntimeException("Alfabeto possui caracteres repetidos");
			}

			// Armazena o caractere
			caracteres.append(caractereAtual, caractere);

			// Desenha cada um dos caracteres
			canvas.drawText(caracteresDisponiveis, i, 1, xDoTextoNaLinhaAtual, yDaLinhaAtual + yDentroDaLinhaDeTexto, paintTexto);

			// + 1 por causa do nosso pixel extra entre as colunas
			xDoTextoNaLinhaAtual += larguraDesseCaractere + 1.0f;
			caracteresNessaLinha++;

			if (caracteresNessaLinha >= quantidadeDeColunas) {
				// Reinicia tudo para começar uma nova linha
				caracteresNessaLinha = 0;
				xDoTextoNaLinhaAtual = 0.0f;

				// + 1 por causa do nosso pixel extra entre as linhas
				yDaLinhaAtual += alturaDeUmaLinha + 1.0f;
			}
		}

		setImagem(new Imagem(bitmap));
		setCaracteres(caracteres);
		setAlturaDaLinha(alturaDeUmaLinha);
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada
		// Contudo, um alerta: ao liberar a memória do alfabeto, todos os textos criados a partir
		// dele ficarão inválidos, e não poderão mais ser desenhados!

		getImagem().destrua();
		setImagem(null);

		getCaracteres().clear();
		setCaracteres(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setFonte(null);
		setCaracteresDisponiveis(null);
	}
}
