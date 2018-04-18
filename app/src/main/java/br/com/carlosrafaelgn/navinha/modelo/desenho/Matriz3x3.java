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
package br.com.carlosrafaelgn.navinha.modelo.desenho;

public final class Matriz3x3 {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	// O OpenGL exige que as matrizes estejam aramzenadas em column-major order, ou seja, em colunas
	// Assim, se montássemos uma matriz com os índices desse vetor, a matriz ficaria assim:
	//
	// | 0 3 6 |
	// | 1 4 7 |
	// | 2 5 8 |
	private final float[] valores;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Matriz3x3() {
		valores = new float[9];

		identidade();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public float[] getValores() {
		return valores;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public Matriz3x3 copie(Matriz3x3 matriz) {
		System.arraycopy(matriz.getValores(), 0, getValores(), 0, 9);

		return this;
	}

	public float determinante() {
		// Calcula o determinante da matriz

		float[] valores = getValores();

		float a = valores[0], b = valores[3], c = valores[6],
			d = valores[1], e = valores[4], f = valores[7],
			g = valores[2], h = valores[5], i = valores[8];

		return ((a * ((e * i) - (f * h))) + (b * ((f * g) - (i * d))) + (c * ((d * h) - (e * g))));
	}

	public Matriz3x3 inversa() {
		// Calcula a matriz inversa dessa matriz

		float[] valores = getValores();

		// O código do determinante foi copiado aqui por questões de desempenho
		float a = valores[0], b = valores[3], c = valores[6],
			d = valores[1], e = valores[4], f = valores[7],
			g = valores[2], h = valores[5], i = valores[8],
			invdet = 1.0f / ((a * ((e * i) - (f * h))) + (b * ((f * g) - (i * d))) + (c * ((d * h) - (e * g))));

		valores[0] = ((e * i) - (f * h)) * invdet;
		valores[1] = ((f * g) - (d * i)) * invdet;
		valores[2] = ((d * h) - (e * g)) * invdet;
		valores[4] = ((c * h) - (b * i)) * invdet;
		valores[5] = ((a * i) - (c * g)) * invdet;
		valores[6] = ((g * b) - (a * h)) * invdet;
		valores[8] = ((b * f) - (c * e)) * invdet;
		valores[9] = ((c * d) - (a * f)) * invdet;
		valores[10] = ((a * e) - (b * d)) * invdet;

		return this;
	}

	public Matriz3x3 transposta() {
		// Calcula a matriz transposta dessa matriz

		float[] valores = getValores();
		float temporario;

		temporario = valores[1];
		valores[1] = valores[3];
		valores[3] = temporario;

		temporario = valores[2];
		valores[2] = valores[6];
		valores[6] = temporario;

		temporario = valores[5];
		valores[5] = valores[7];
		valores[7] = temporario;

		return this;
	}

	public Matriz3x3 identidade() {
		float[] valores = getValores();

		// Coluna 0
		valores[0] = 1.0f;
		valores[1] = 0.0f;
		valores[2] = 0.0f;

		// Coluna 1
		valores[3] = 0.0f;
		valores[4] = 1.0f;
		valores[5] = 0.0f;

		// Coluna 2
		valores[6] = 0.0f;
		valores[7] = 0.0f;
		valores[8] = 1.0f;

		return this;
	}

	public Matriz3x3 translacao(float x, float y) {
		// Cria uma matriz de translação
		//
		// this = Matriz de translação

		float[] valores = getValores();

		// Coluna 0
		valores[0] = 1;
		valores[1] = 0;
		valores[2] = 0;

		// Coluna 1
		valores[3] = 0;
		valores[4] = 1;
		valores[5] = 0;

		// Coluna 2
		valores[6] = x;
		valores[7] = y;
		valores[8] = 1;

		return this;
	}

	public Matriz3x3 preTranslacao(float x, float y) {
		// Multiplica a matriz por uma matriz de translação, de modo que a translação será
		// visualmente aplicada antes das outras transformações já presentes nessa matriz
		//
		// this = this * Matriz de translação

		float[] valores = getValores();

		valores[6] += (valores[0] * x) + (valores[3] * y);
		valores[7] += (valores[1] * x) + (valores[4] * y);
		valores[8] += (valores[2] * x) + (valores[5] * y);

		return this;
	}

	public Matriz3x3 posTranslacao(float x, float y) {
		// Multiplica a matriz por uma matriz de translação, de modo que a translação será
		// visualmente aplicada depois das outras transformações já presentes nessa matriz
		//
		// this = Matriz de translação * this

		float[] valores = getValores();

		float valor2 = valores[2], valor5 = valores[5], valor8 = valores[8];

		// Linha 0
		valores[0] += x * valor2;
		valores[3] += x * valor5;
		valores[6] += x * valor8;

		// Linha 1
		valores[1] += y * valor2;
		valores[4] += y * valor5;
		valores[7] += y * valor8;

		return this;
	}

	public Matriz3x3 escala(float x, float y) {
		// Cria uma matriz com fator de escala
		//
		// this = Matriz com fator de escala

		float[] valores = getValores();

		// Coluna 0
		valores[0] = x;
		valores[1] = 0.0f;
		valores[2] = 0.0f;

		// Coluna 1
		valores[3] = 0.0f;
		valores[4] = y;
		valores[5] = 0.0f;

		// Coluna 2
		valores[6] = 0.0f;
		valores[7] = 0.0f;
		valores[8] = 1.0f;

		return this;
	}

	public Matriz3x3 preEscala(float x, float y) {
		// Multiplica a matriz por uma matriz com fator de escala, de modo que a escala será
		// visualmente aplicada antes das outras transformações já presentes nessa matriz
		//
		// this = this * Matriz com fator de escala

		float[] valores = getValores();

		// Coluna 0
		valores[0] *= x;
		valores[1] *= x;
		valores[2] *= x;

		// Coluna 1
		valores[3] *= y;
		valores[4] *= y;
		valores[5] *= y;

		return this;
	}

	public Matriz3x3 posEscala(float x, float y) {
		// Multiplica a matriz por uma matriz com fator de escala, de modo que a escala será
		// visualmente aplicada depois das outras transformações já presentes nessa matriz
		//
		// this = Matriz com fator de escala * this

		float[] valores = getValores();

		// Linha 0
		valores[0] *= x;
		valores[3] *= x;
		valores[6] *= x;

		// Linha 1
		valores[1] *= y;
		valores[4] *= y;
		valores[7] *= y;

		return this;
	}

	public Matriz3x3 rotacao(float anguloEmRadianos) {
		// Cria uma matriz de rotação sobre o eixo Z
		//
		// this = Matriz de rotação

		float[] valores = getValores();
		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos);

		// Coluna 0
		valores[0] = cos;
		valores[1] = sen;
		valores[2] = 0;

		// Coluna 1
		valores[3] = -sen;
		valores[4] = cos;
		valores[5] = 0;

		// Coluna 2
		valores[6] = 0;
		valores[7] = 0;
		valores[8] = 1;

		return this;
	}

	public Matriz3x3 preRotacao(float anguloEmRadianos) {
		// Multiplica a matriz por uma matriz de rotação, de modo que a escala será visualmente
		// aplicada antes das outras transformações já presentes nessa matriz
		//
		// this = this * Matriz de rotação

		float[] valores = getValores();
		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos), a, b;

		a = valores[0];
		b = valores[3];
		valores[0] = (a * cos) + (b * sen);
		valores[3] = (b * cos) - (a * sen);
		a = valores[1];
		b = valores[4];
		valores[1] = (a * cos) + (b * sen);
		valores[4] = (b * cos) - (a * sen);
		a = valores[2];
		b = valores[5];
		valores[2] = (a * cos) + (b * sen);
		valores[5] = (b * cos) - (a * sen);

		return this;
	}

	public Matriz3x3 posRotacao(float anguloEmRadianos) {
		// Multiplica a matriz por uma matriz de rotação, de modo que a escala será visualmente
		// aplicada depois das outras transformações já presentes nessa matriz
		//
		// this = Matriz de rotação * this

		float[] valores = getValores();
		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos), a, b;

		// Coluna 0
		a = valores[0];
		b = valores[1];
		valores[0] = (cos * a) - (sen * b);
		valores[1] = (sen * a) + (cos * b);

		// Coluna 1
		a = valores[3];
		b = valores[4];
		valores[3] = (cos * a) - (sen * b);
		valores[4] = (sen * a) + (cos * b);

		// Coluna 2
		a = valores[6];
		b = valores[7];
		valores[6] = (cos * a) - (sen * b);
		valores[7] = (sen * a) + (cos * b);

		return this;
	}

	public Matriz3x3 posVista(float doisSobreLargura, float menosDoisSobreAltura) {
		// Multiplica a matriz por uma matriz que passará os valores da tela, com coordenadas indo
		// de (0, 0) até (largura, altura), para o espaço do OpenGL com coordenadas de (-1, 1) até
		// (1, -1)
		//
		// Tem o mesmo efeito dessa sequência de chamadas:
		// this.posEscala(doisSobreLargura, menosDoisSobreAltura).posTranslacao(-1.0f, 1.0f);

		float[] valores = getValores();

		float valor2 = valores[2], valor5 = valores[5], valor8 = valores[8];

		// Linha 0
		valores[0] = (valores[0] * doisSobreLargura) - valor2;
		valores[3] = (valores[3] * doisSobreLargura) - valor5;
		valores[6] = (valores[6] * doisSobreLargura) - valor8;

		// Linha 1
		valores[1] = (valores[1] * menosDoisSobreAltura) + valor2;
		valores[4] = (valores[4] * menosDoisSobreAltura) + valor5;
		valores[7] = (valores[7] * menosDoisSobreAltura) + valor8;

		return this;
	}

	// Os métodos abaixo são versões mais rápidas dos métodos acima, e todos partem de uma premissa:
	// os valores 2 e 5 sempre serão 0, e o valor 8 sempre será 1
	// Se alguma dessas premissas não for verdadeira, os resultados desses métodos estarão ERRADOS!

	public Matriz3x3 translacaoOt(float x, float y) {
		// Cria uma matriz de translação
		//
		// this = Matriz de translação

		float[] valores = getValores();

		// Coluna 0
		valores[0] = 1;
		valores[1] = 0;

		// Coluna 1
		valores[3] = 0;
		valores[4] = 1;

		// Coluna 2
		valores[6] = x;
		valores[7] = y;

		return this;
	}

	public Matriz3x3 preTranslacaoOt(float x, float y) {
		// Multiplica a matriz por uma matriz de translação, de modo que a translação será
		// visualmente aplicada antes das outras transformações já presentes nessa matriz
		//
		// this = this * Matriz de translação

		float[] valores = getValores();

		valores[6] += (valores[0] * x) + (valores[3] * y);
		valores[7] += (valores[1] * x) + (valores[4] * y);

		return this;
	}

	public Matriz3x3 posTranslacaoOt(float x, float y) {
		// Multiplica a matriz por uma matriz de translação, de modo que a translação será
		// visualmente aplicada depois das outras transformações já presentes nessa matriz
		//
		// this = Matriz de translação * this

		float[] valores = getValores();

		// Linha 0
		valores[6] += x;

		// Linha 1
		valores[7] += y;

		return this;
	}

	public Matriz3x3 escalaOt(float x, float y) {
		// Cria uma matriz com fator de escala
		//
		// this = Matriz com fator de escala

		float[] valores = getValores();

		// Coluna 0
		valores[0] = x;
		valores[1] = 0.0f;

		// Coluna 1
		valores[3] = 0.0f;
		valores[4] = y;

		// Coluna 2
		valores[6] = 0.0f;
		valores[7] = 0.0f;

		return this;
	}

	public Matriz3x3 preEscalaOt(float x, float y) {
		// Multiplica a matriz por uma matriz com fator de escala, de modo que a escala será
		// visualmente aplicada antes das outras transformações já presentes nessa matriz
		//
		// this = this * Matriz com fator de escala

		float[] valores = getValores();

		// Coluna 0
		valores[0] *= x;
		valores[1] *= x;

		// Coluna 1
		valores[3] *= y;
		valores[4] *= y;

		return this;
	}

	public Matriz3x3 rotacaoOt(float anguloEmRadianos) {
		// Cria uma matriz de rotação sobre o eixo Z
		//
		// this = Matriz de rotação

		float[] valores = getValores();
		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos);

		// Coluna 0
		valores[0] = cos;
		valores[1] = sen;

		// Coluna 1
		valores[3] = -sen;
		valores[4] = cos;

		return this;
	}

	public Matriz3x3 preRotacaoOt(float anguloEmRadianos) {
		// Multiplica a matriz por uma matriz de rotação, de modo que a escala será visualmente
		// aplicada antes das outras transformações já presentes nessa matriz
		//
		// this = this * Matriz de rotação

		float[] valores = getValores();
		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos), a, b;

		a = valores[0];
		b = valores[3];
		valores[0] = (a * cos) + (b * sen);
		valores[3] = (b * cos) - (a * sen);
		a = valores[1];
		b = valores[4];
		valores[1] = (a * cos) + (b * sen);
		valores[4] = (b * cos) - (a * sen);

		return this;
	}

	public Matriz3x3 posVistaOt(float doisSobreLargura, float menosDoisSobreAltura) {
		// Multiplica a matriz por uma matriz que passará os valores da tela, com coordenadas indo
		// de (0, 0) até (largura, altura), para o espaço do OpenGL com coordenadas de (-1, 1) até
		// (1, -1)
		//
		// Tem o mesmo efeito dessa sequência de chamadas:
		// this.posEscalaOt(doisSobreLargura, menosDoisSobreAltura).posTranslacaoOt(-1.0f, 1.0f);

		float[] valores = getValores();

		// Linha 0
		valores[0] = (valores[0] * doisSobreLargura);
		valores[3] = (valores[3] * doisSobreLargura);
		valores[6] = (valores[6] * doisSobreLargura) - 1.0f;

		// Linha 1
		valores[1] = (valores[1] * menosDoisSobreAltura);
		valores[4] = (valores[4] * menosDoisSobreAltura);
		valores[7] = (valores[7] * menosDoisSobreAltura) + 1.0f;

		return this;
	}
}
