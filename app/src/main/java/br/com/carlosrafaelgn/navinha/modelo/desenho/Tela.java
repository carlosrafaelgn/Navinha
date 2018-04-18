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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

public final class Tela {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	public static final int ROTACAO_0 = 0;
	public static final int ROTACAO_90 = 1;
	public static final int ROTACAO_180 = 2;
	public static final int ROTACAO_270 = 3;

	private static final int CAPACIDADE_DE_RETANGULOS_INICIAL = 1000;

	// Cada vértice nosso terá 5 atributos: x e y da posição, alpha, x e y da textura, e cada
	// atributo possui 4 bytes (1 float = 4 bytes)
	private static final int FLOATS_POR_POSICAO = 2;
	private static final int FLOATS_POR_ALPHA = 1;
	private static final int FLOATS_POR_COORDENADAS_DE_TEXTURA = 2;

	private static final int FLOATS_POR_VERTICE = (FLOATS_POR_POSICAO + FLOATS_POR_ALPHA + FLOATS_POR_COORDENADAS_DE_TEXTURA);
	private static final int BYTES_POR_VERTICE = 4 * FLOATS_POR_VERTICE;

	private static final int FLOATS_POR_RETANGULO = 4 * FLOATS_POR_VERTICE;
	private static final int BYTES_POR_RETANGULO = 4 * FLOATS_POR_RETANGULO;

	// Os índices de início de cada atributo dentro do buffer
	// A posição vem primeiro
	private static final int INICIO_POSICAO = 0;
	// O alpha vem em segundo, por isso "pulamos" o x e o y da posição
	private static final int INICIO_ALPHA = 2 * 4;
	// Por fim temos as coordenadas de textura, onde "pulamos" o x e o y da posição + o alpha
	private static final int INICIO_COORDENADAS_DE_TEXTURA = 4 * (2 + 1);

	// Utilizados para mapear os atributos aPosicao, aAlpha e aCoordenadasDeTextura do shader
	private static final int ATRIBUTO_POSICAO = 0;
	private static final int ATRIBUTO_ALPHA = 1;
	private static final int ATRIBUTO_COORDENADAS_DE_TEXTURA = 2;

	// Nosso shader de vértice (o alpha não é um uniform pois ele varia de sprite para sprite, e
	// nós vamos querer desenhar vários retângulos/sprites de uma só vez)
	private static final String CODIGO_DO_SHADER_DE_VERTICE = "attribute vec2 aPosicao;\n"+
		"attribute float aAlpha;\n"+
		"attribute vec2 aCoordenadasDeTextura;\n" +
		"varying float vAlpha;\n" +
		"varying vec2 vCoordenadasDeTextura;\n" +
		"uniform float doisSobreLargura, menosDoisSobreAltura;\n" +
		"void main() {\n" +
		"   gl_Position = vec4((aPosicao.x * doisSobreLargura) - 1.0, (aPosicao.y * menosDoisSobreAltura) + 1.0, 0.0, 1.0);\n" +
		"   vAlpha = aAlpha;\n" +
		"   vCoordenadasDeTextura = aCoordenadasDeTextura;\n" +
		"}";

	// Nosso shader de fragmento
	private static final String CODIGO_DO_SHADER_DE_FRAGMENTO = "precision mediump float;\n" +
		"varying float vAlpha;\n" +
		"varying vec2 vCoordenadasDeTextura;\n" +
		"uniform sampler2D textura;\n" +
		"void main() {\n" +
		"   vec4 cor = texture2D(textura, vCoordenadasDeTextura);\n" +
		"   cor.a *= vAlpha;\n" +
		"   gl_FragColor = cor;\n" +
		"}";

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	// A Tela utiliza o padrão Singleton, onde só existe uma instância do objeto no sistema inteiro
	// (perceba que para reforçar isso, o construtor da classe é privado)
	private static final Tela tela = new Tela();

	private Typeface fonte8Bit;
	private TextPaint paintTexto;
	private Paint.FontMetrics metricasDoTexto;
	private int rotacao;
	// Armazena a largura e a altura como float para auxiliar o OpenGL
	private float larguraDaTela, alturaDaTela, larguraDaVista, alturaDaVista, fatorTelaVistaX, fatorTelaVistaY, densidade;
	private int[] idTemporario;
	private int idDoPrograma, idDoShaderDeVertice, idDoShaderDeFragmento, idDoBufferDeVertices, idDoBufferDeIndices;
	private Imagem imagemDosRetangulosPendentes;
	private float[] vertices;
	private FloatBuffer bufferDeVertices;
	private int contagemDeRetangulos, capacidadeDeRetangulos, capacidadeDeRetangulosDoBuffer;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	private Tela() {
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public static Tela getTela() {
		return tela;
	}

	public Typeface getFonte8Bit() {
		return fonte8Bit;
	}

	private void setFonte8Bit(Typeface fonte8Bit) {
		this.fonte8Bit = fonte8Bit;
	}

	private TextPaint getPaintTexto() {
		return paintTexto;
	}

	private void setPaintTexto(TextPaint paintTexto) {
		this.paintTexto = paintTexto;
	}

	private Paint.FontMetrics getMetricasDoTexto() {
		return metricasDoTexto;
	}

	private void setMetricasDoTexto(Paint.FontMetrics metricasDoTexto) {
		this.metricasDoTexto = metricasDoTexto;
	}

	public int getRotacao() {
		return rotacao;
	}

	private void setRotacao(int rotacao) {
		this.rotacao = rotacao;
	}

	public float getLarguraDaTela() {
		return larguraDaTela;
	}

	private void setLarguraDaTela(float larguraDaTela) {
		this.larguraDaTela = larguraDaTela;
	}

	public float getAlturaDaTela() {
		return alturaDaTela;
	}

	private void setAlturaDaTela(float alturaDaTela) {
		this.alturaDaTela = alturaDaTela;
	}

	public float getLarguraDaVista() {
		return larguraDaVista;
	}

	public void setLarguraDaVista(float larguraDaVista) {
		this.larguraDaVista = larguraDaVista;

		setFatorTelaVistaX(larguraDaVista / getLarguraDaTela());

		// Atualiza o uniform doisSobreLargura do programa, caso ele exista
		int idDoPrograma = getIdDoPrograma();
		if (idDoPrograma != 0) {
			GLES20.glUniform1f(GLES20.glGetUniformLocation(getIdDoPrograma(), "doisSobreLargura"), 2.0f / larguraDaVista);
		}
	}

	public float getAlturaDaVista() {
		return alturaDaVista;
	}

	public void setAlturaDaVista(float alturaDaVista) {
		this.alturaDaVista = alturaDaVista;

		setFatorTelaVistaY(alturaDaVista / getAlturaDaTela());

		// Atualiza o uniform menosDoisSobreAltura do programa, caso ele exista
		int idDoPrograma = getIdDoPrograma();
		if (idDoPrograma != 0) {
			GLES20.glUniform1f(GLES20.glGetUniformLocation(getIdDoPrograma(), "menosDoisSobreAltura"), -2.0f / alturaDaVista);
		}
	}

	public float getFatorTelaVistaX() {
		return fatorTelaVistaX;
	}

	private void setFatorTelaVistaX(float fatorTelaVistaX) {
		this.fatorTelaVistaX = fatorTelaVistaX;
	}

	public float getFatorTelaVistaY() {
		return fatorTelaVistaY;
	}

	private void setFatorTelaVistaY(float fatorTelaVistaY) {
		this.fatorTelaVistaY = fatorTelaVistaY;
	}

	public float getDensidade() {
		return densidade;
	}

	private void setDensidade(float densidade) {
		this.densidade = densidade;
	}

	private int[] getIdTemporario() {
		return idTemporario;
	}

	private void setIdTemporario(int[] idTemporario) {
		this.idTemporario = idTemporario;
	}

	private int getIdDoPrograma() {
		return idDoPrograma;
	}

	private void setIdDoPrograma(int idDoPrograma) {
		this.idDoPrograma = idDoPrograma;
	}

	private int getIdDoShaderDeVertice() {
		return idDoShaderDeVertice;
	}

	private void setIdDoShaderDeVertice(int idDoShaderDeVertice) {
		this.idDoShaderDeVertice = idDoShaderDeVertice;
	}

	private int getIdDoShaderDeFragmento() {
		return idDoShaderDeFragmento;
	}

	private void setIdDoShaderDeFragmento(int idDoShaderDeFragmento) {
		this.idDoShaderDeFragmento = idDoShaderDeFragmento;
	}

	private int getIdDoBufferDeVertices() {
		return idDoBufferDeVertices;
	}

	private void setIdDoBufferDeVertices(int idDoBufferDeVertices) {
		this.idDoBufferDeVertices = idDoBufferDeVertices;
	}

	private int getIdDoBufferDeIndices() {
		return idDoBufferDeIndices;
	}

	private void setIdDoBufferDeIndices(int idDoBufferDeIndices) {
		this.idDoBufferDeIndices = idDoBufferDeIndices;
	}

	private Imagem getImagemDosRetangulosPendentes() {
		return imagemDosRetangulosPendentes;
	}

	private void setImagemDosRetangulosPendentes(Imagem imagemDosRetangulosPendentes) {
		this.imagemDosRetangulosPendentes = imagemDosRetangulosPendentes;
	}

	private float[] getVertices() {
		return vertices;
	}

	private void setVertices(float[] vertices) {
		this.vertices = vertices;
	}

	private FloatBuffer getBufferDeVertices() {
		return bufferDeVertices;
	}

	private void setBufferDeVertices(FloatBuffer bufferDeVertices) {
		this.bufferDeVertices = bufferDeVertices;
	}

	private int getContagemDeRetangulos() {
		return contagemDeRetangulos;
	}

	private void setContagemDeRetangulos(int contagemDeRetangulos) {
		this.contagemDeRetangulos = contagemDeRetangulos;
	}

	private int getCapacidadeDeRetangulos() {
		return capacidadeDeRetangulos;
	}

	private void setCapacidadeDeRetangulos(int capacidadeDeRetangulos) {
		this.capacidadeDeRetangulos = capacidadeDeRetangulos;
	}

	private int getCapacidadeDeRetangulosDoBuffer() {
		return capacidadeDeRetangulosDoBuffer;
	}

	private void setCapacidadeDeRetangulosDoBuffer(int capacidadeDeRetangulosDoBuffer) {
		this.capacidadeDeRetangulosDoBuffer = capacidadeDeRetangulosDoBuffer;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private int indiceDoProximoVerticeDosRetangulos() {
		int contagemDeRetangulos = getContagemDeRetangulos();

		// Se precisar aumentar a capacidade, aumenta em passos de 32 por vez
		if (contagemDeRetangulos >= getCapacidadeDeRetangulos()) {
			int capacidadeDeRetangulosDesejada = contagemDeRetangulos + 32;

			setCapacidadeDeRetangulos(capacidadeDeRetangulosDesejada);

			setVertices(Arrays.copyOf(getVertices(), FLOATS_POR_RETANGULO * capacidadeDeRetangulosDesejada));
		}

		setContagemDeRetangulos(contagemDeRetangulos + 1);

		// Retorna o índice do primeiro vértice do próximo retângulo que deve ser preenchido
		return FLOATS_POR_RETANGULO * contagemDeRetangulos;
	}

	private void desenheRetangulosPendentes() {
		int contagemDeRetangulos = getContagemDeRetangulos();

		if (contagemDeRetangulos == 0) {
			return;
		}

		if (contagemDeRetangulos > getCapacidadeDeRetangulosDoBuffer()) {
			// Se a capacidade não for suficiente, realoca os buffers
			garantaCapacidadeDeRetangulos(getCapacidadeDeRetangulos());
		} else {
			// Quando a capacidade for suficiente, basta atualizar os dados dos vértices

			// Define o buffer do lote de retângulos como o buffer atual do OpenGL
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, getIdDoBufferDeVertices());
			GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, BYTES_POR_RETANGULO * contagemDeRetangulos,
				getBufferDeVertices().put(getVertices()).position(0));
		}

		// Especifica a textura que o OpenGL utilizará para desenhar o retângulo
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getImagemDosRetangulosPendentes().getId());

		// Explica para o OpenGL de onde ele deve tirar os dados dos atributos dos vértices
		GLES20.glVertexAttribPointer(Tela.ATRIBUTO_POSICAO, FLOATS_POR_POSICAO, GLES20.GL_FLOAT, false, BYTES_POR_VERTICE, INICIO_POSICAO);
		GLES20.glVertexAttribPointer(Tela.ATRIBUTO_ALPHA, FLOATS_POR_ALPHA, GLES20.GL_FLOAT, false, BYTES_POR_VERTICE, INICIO_ALPHA);
		GLES20.glVertexAttribPointer(Tela.ATRIBUTO_COORDENADAS_DE_TEXTURA, FLOATS_POR_COORDENADAS_DE_TEXTURA, GLES20.GL_FLOAT, false, BYTES_POR_VERTICE, INICIO_COORDENADAS_DE_TEXTURA);

		// Especifica os índices dos triângulos que formam os retângulos
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, getIdDoBufferDeIndices());

		// Cada modelo é formado por 2 triângulos, e cada triângulo possui 3 índices
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, (6 * contagemDeRetangulos), GLES20.GL_UNSIGNED_SHORT, 0);

		// Como todos os retângulos já foram desenhados, então basta zerar a contagem
		setContagemDeRetangulos(0);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public boolean telaAlterada(Context context, int larguraDaTela, int alturaDaTela) {
		if ((int)getLarguraDaTela() == larguraDaTela && (int)getAlturaDaTela() == alturaDaTela) {
			return false;
		}

		destrua();

		// Cria a fonte que será utilizada para escrever na tela
		// No caso, essa fonte é a fonte Press Start 2P, gratuita, licenciada pela SIL OPEN FONT
		// LICENSE Version 1.1
		// Copyright (c) 2012, Cody "CodeMan38" Boisclair (cody@zone38.net)
		// Disponível nesses dois endereços:
		// https://www.google.com/fonts/specimen/Press+Start+2P
		// https://github.com/google/fonts/tree/master/ofl/pressstart2p
		setFonte8Bit(Typeface.createFromAsset(context.getAssets(), "fonts/PressStart2P_Regular.ttf"));

		// Cria as configurações iniciais dos texto
		TextPaint paintTexto = new TextPaint();
		setPaintTexto(paintTexto);
		paintTexto.setAntiAlias(false);
		paintTexto.setDither(false);
		paintTexto.setStyle(Paint.Style.FILL);
		paintTexto.setTextAlign(Paint.Align.LEFT);
		paintTexto.setTypeface(getFonte8Bit());

		setMetricasDoTexto(paintTexto.getFontMetrics());

		Display telaDoDispositivo = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		// Determina a rotação da tela do dispositivo (será algum dos valores ROTACAO_XXX)
		setRotacao(telaDoDispositivo.getRotation());

		// Descobre a densidade de pixels da tela
		DisplayMetrics displayMetrics = new DisplayMetrics();
		telaDoDispositivo.getMetrics(displayMetrics);
		setDensidade(displayMetrics.density);

		// Aloca o id temporário, utilizado com os métodos do OpenGL
		int[] idTemporario = new int[1];

		setIdTemporario(idTemporario);

		// De agora em diante: OpenGL ES :)

		// Limpa as marcações de erro do OpenGL
		while (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			GLES20.glGetError();
		}

		// Cria um programa
		int idDoPrograma = GLES20.glCreateProgram();
		setIdDoPrograma(idDoPrograma);
		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idDoPrograma == 0) {
			throw new RuntimeException("Não foi possível criar o programa");
		}

		// Cria e compila o shader de vértice
		int idDoShaderDeVertice = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		setIdDoShaderDeVertice(idDoShaderDeVertice);
		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idDoShaderDeVertice == 0) {
			throw new RuntimeException("Não foi possível criar o shader de vértice");
		}
		GLES20.glShaderSource(idDoShaderDeVertice, CODIGO_DO_SHADER_DE_VERTICE);
		GLES20.glCompileShader(idDoShaderDeVertice);
		GLES20.glGetShaderiv(idDoShaderDeVertice, GLES20.GL_COMPILE_STATUS, idTemporario, 0);
		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idTemporario[0] == 0) {
			throw new RuntimeException("Não foi possível compilar o shader de vértice");
		}

		// Cria e compila o shader de fragmento
		int idDoShaderDeFragmento = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		setIdDoShaderDeFragmento(idDoShaderDeFragmento);
		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idDoShaderDeFragmento == 0) {
			throw new RuntimeException("Não foi possível criar o shader de fragmento");
		}
		GLES20.glShaderSource(idDoShaderDeFragmento, CODIGO_DO_SHADER_DE_FRAGMENTO);
		GLES20.glCompileShader(idDoShaderDeFragmento);
		GLES20.glGetShaderiv(idDoShaderDeFragmento, GLES20.GL_COMPILE_STATUS, idTemporario, 0);
		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idTemporario[0] == 0) {
			throw new RuntimeException("Não foi possível compilar o shader de fragmento");
		}

		// Associa os dois shaders ao programa criado
		GLES20.glAttachShader(idDoPrograma, idDoShaderDeVertice);
		GLES20.glAttachShader(idDoPrograma, idDoShaderDeFragmento);

		// Define a localização dos atributos inPosicaoAlpha e inCoordenadasDeTextura
		GLES20.glBindAttribLocation(idDoPrograma, ATRIBUTO_POSICAO, "aPosicao");
		GLES20.glBindAttribLocation(idDoPrograma, ATRIBUTO_ALPHA, "aAlpha");
		GLES20.glBindAttribLocation(idDoPrograma, ATRIBUTO_COORDENADAS_DE_TEXTURA, "aCoordenadasDeTextura");

		// Tarefa final!
		GLES20.glLinkProgram(idDoPrograma);
		GLES20.glUseProgram(idDoPrograma);

		if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Não foi possível finalizar a criação do programa");
		}

		// Define a textura a ser utilizada pelo programa como a textura 0 (a partir daí, só
		// precisaremos alterar quem é a textura 0)
		// Se o programa utilizasse mais de uma textura, sempre antes de chamar
		// GLES20.glBindTexture(), precisaríamos chamar GLES20.glActiveTexture(GLES20.GL_TEXTUREX)
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glUniform1i(GLES20.glGetUniformLocation(idDoPrograma, "textura"), 0);

		// Habilita o uso de buffers para os enviar os dados dos atributos aPosicao, aAlpha e
		// aCoordenadasDeTextura durante o desenho das primitivas, realizado por métodos como
		// glDrawArrays ou glDrawElements
		GLES20.glEnableVertexAttribArray(ATRIBUTO_POSICAO);
		GLES20.glEnableVertexAttribArray(ATRIBUTO_ALPHA);
		GLES20.glEnableVertexAttribArray(ATRIBUTO_COORDENADAS_DE_TEXTURA);

		if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL");
		}

		// Configuração básica do OpenGL
		// Essas configurações são adequadas para nosso jogo, mas podem não servir para todos os
		// jogos (por exemplo, ao desabilitar o GL_DEPTH_TEST, objetos mais distantes podem aparecer
		// visualmente na frente de objetos mais próximos)
		int err;
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		if ((err = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL: " + err);
		}
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		if ((err = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL: " + err);
		}
		GLES20.glDisable(GLES20.GL_DITHER);
		if ((err = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL: " + err);
		}
		GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
		if ((err = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL: " + err);
		}
		GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);
		GLES20.glDisable(GLES20.GL_SAMPLE_ALPHA_TO_COVERAGE);
		GLES20.glDisable(GLES20.GL_SAMPLE_COVERAGE);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		if ((err = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL: " + err);
		}

		// Precisamos deixar GL_BLEND ligado para que a transparência dos sprites funcione
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Algo saiu errado durante a configuração do OpenGL");
		}

		// Faz com que a vista ocupe a área da tela inteira
		setLarguraDaTela((float)larguraDaTela);
		setAlturaDaTela((float)alturaDaTela);
		setLarguraDaVista((float)larguraDaTela);
		setAlturaDaVista((float)alturaDaTela);

		// Define a área da janela utilizada pelo OpenGL como sendo a tela toda
		GLES20.glViewport(0, 0, larguraDaTela, alturaDaTela);

		// Por fim, vamos criar as estruturas utilizadas para desenhar os retângulos na tela
		setIdDoBufferDeVertices(crieUmBuffer());
		setIdDoBufferDeIndices(crieUmBuffer());
		garantaCapacidadeDeRetangulos(CAPACIDADE_DE_RETANGULOS_INICIAL);

		return true;
	}

	public void destrua() {
		// Libera todos os objetos do OpenGL

		int id;

		id = getIdDoPrograma();
		if (id != 0) {
			GLES20.glDeleteProgram(id);
			setIdDoPrograma(0);
		}

		id = getIdDoShaderDeVertice();
		if (id != 0) {
			GLES20.glDeleteShader(id);
			setIdDoPrograma(0);
		}

		id = getIdDoShaderDeFragmento();
		if (id != 0) {
			GLES20.glDeleteShader(id);
			setIdDoPrograma(0);
		}

		destruaUmBuffer(getIdDoBufferDeVertices());
		setIdDoBufferDeVertices(0);

		destruaUmBuffer(getIdDoBufferDeIndices());
		setIdDoBufferDeIndices(0);

		// Define as propriedades da tela para valores inválidos, para garantir que essa tela não
		// seja mais utilizada
		setLarguraDaTela(-1.0f);
		setAlturaDaTela(-1.0f);
		setLarguraDaVista(-1.0f);
		setAlturaDaVista(-1.0f);
		setFonte8Bit(null);
		setPaintTexto(null);
		setMetricasDoTexto(null);
		setIdTemporario(null);
		setImagemDosRetangulosPendentes(null);
		setVertices(null);
		setBufferDeVertices(null);
		setContagemDeRetangulos(0);
		setCapacidadeDeRetangulos(0);
		setCapacidadeDeRetangulosDoBuffer(0);
	}

	public void termineOQuadro() {
		// Ao final do quadro, precisamos desenhar os retângulos que ficaram pendentes

		desenheRetangulosPendentes();

		GLES20.glFlush();
	}

	public int crieUmaTextura() {
		// Limpa as marcações de erro do OpenGL
		while (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			GLES20.glGetError();
		}

		int[] idTemporario = getIdTemporario();

		GLES20.glGenTextures(1, idTemporario, 0);

		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idTemporario[0] == 0) {
			throw new RuntimeException("Erro ao criar a textura");
		}

		return idTemporario[0];
	}

	public void destruaUmaTextura(int idDaTextura) {
		if (idDaTextura == 0) {
			return;
		}

		int[] idTemporario = getIdTemporario();

		idTemporario[0] = idDaTextura;

		GLES20.glDeleteTextures(1, idTemporario, 0);
	}

	public int crieUmBuffer() {
		// Limpa as marcações de erro do OpenGL
		while (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			GLES20.glGetError();
		}

		int[] idTemporario = getIdTemporario();

		GLES20.glGenBuffers(1, idTemporario, 0);

		if (GLES20.glGetError() != GLES20.GL_NO_ERROR || idTemporario[0] == 0) {
			throw new RuntimeException("Erro ao criar o buffer");
		}

		return idTemporario[0];
	}

	public void destruaUmBuffer(int idDoBuffer) {
		if (idDoBuffer == 0) {
			return;
		}

		int[] idTemporario = getIdTemporario();

		idTemporario[0] = idDoBuffer;

		GLES20.glDeleteBuffers(1, idTemporario, 0);
	}

	public void corDoPreenchimento(float vermelho, float verde, float azul) {
		GLES20.glClearColor(vermelho, verde, azul, 1.0f);
	}

	public void preencha() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
	}

	public int larguraDoTexto(String texto, int tamanhoDoTexto) {
		getPaintTexto().setTextSize(tamanhoDoTexto);
		return (int)(getPaintTexto().measureText(texto) + 0.99f);
	}

	public int larguraDoTexto(char[] caracteres, int primeiroCaractere, int contagemDeCaracteres, int tamanhoDoTexto) {
		getPaintTexto().setTextSize(tamanhoDoTexto);
		return (int)(getPaintTexto().measureText(caracteres, primeiroCaractere, contagemDeCaracteres) + 0.99f);
	}

	public int alturaDaLinhaDeTexto(int tamanhoDoTexto) {
		Paint.FontMetrics metricas = getMetricasDoTexto();
		getPaintTexto().setTextSize(tamanhoDoTexto);
		getPaintTexto().getFontMetrics(metricas);
		return (int)(metricas.bottom - metricas.top + 0.99f);
	}

	public int posicaoYDentroDaLinhaDeTexto(int tamanhoDoTexto) {
		Paint.FontMetrics metricas = getMetricasDoTexto();
		getPaintTexto().setTextSize(tamanhoDoTexto);
		getPaintTexto().getFontMetrics(metricas);
		return (int)(-metricas.top + 0.99f);
	}

	public Imagem crieImagemDoTexto(String texto, int espacoExtraEntreLinhas, int tamanhoDoTexto, int cor) {
		// Configura as caracteríticas básicas do texto
		TextPaint paintTexto = getPaintTexto();
		paintTexto.setColor(cor);
		paintTexto.setTextSize(tamanhoDoTexto);

		// Vamos contar quantas linhas há em texto
		int quantidadeDeLinhas = 0, inicioDaLinhaAnterior = 0, fimDaLinha;

		do {
			fimDaLinha = texto.indexOf('\n', inicioDaLinhaAnterior);
			quantidadeDeLinhas++;
			inicioDaLinhaAnterior = fimDaLinha + 1;
		} while (fimDaLinha >= 0);

		int[] posicoesDasLinhas = new int[1 + quantidadeDeLinhas];

		// A primeira e a última são as mais fáceis ;)
		posicoesDasLinhas[0] = 0;
		posicoesDasLinhas[posicoesDasLinhas.length - 1] = texto.length() + 1;

		for (int i = 1; i < posicoesDasLinhas.length - 1; i++) {
			// Procura pela próxima quebra de linha a partir do primeiro caractere da linha anterior
			// (O + 1 é para ignorar o caractere \n, que não deve ser desenhado)
			posicoesDasLinhas[i] = texto.indexOf('\n', posicoesDasLinhas[i - 1]) + 1;
		}

		// Mede a largura e a altura do texto, sempre arredondando para cima

		int alturaDeUmaLinha = alturaDaLinhaDeTexto(tamanhoDoTexto);
		int larguraDessaLinha, larguraMaximaDeLinha = 0;

		for (int i = 0; i < posicoesDasLinhas.length - 1; i++) {
			// Mede a largura de cada uma das linhas, e armazena a maior
			larguraDessaLinha = (int)(paintTexto.measureText(texto, posicoesDasLinhas[i], posicoesDasLinhas[i + 1] - 1) + 0.99f);
			if (larguraMaximaDeLinha < larguraDessaLinha) {
				larguraMaximaDeLinha = larguraDessaLinha;
			}
		}

		// Cria um bitmap onde o texto será desenhado
		Bitmap bitmap = Bitmap.createBitmap(larguraMaximaDeLinha, (alturaDeUmaLinha * quantidadeDeLinhas) + ((quantidadeDeLinhas - 1) * espacoExtraEntreLinhas), Bitmap.Config.ARGB_8888);

		// Cria um Canvas com base nesse bitmap, de modo que tudo que fizermos ao Canvas, ficará
		// armazenado dentro do bitmap
		Canvas canvas = new Canvas(bitmap);

		// Posição y de onde desenhar o texto em uma linha
		int yDaLinhaAtual = posicaoYDentroDaLinhaDeTexto(tamanhoDoTexto);

		for (int i = 0; i < posicoesDasLinhas.length - 1; i++) {
			// Desenha cada uma das linhas
			canvas.drawText(texto, posicoesDasLinhas[i], posicoesDasLinhas[i + 1] - 1, 0.0f, yDaLinhaAtual, paintTexto);

			// Pula para a próxima linha
			yDaLinhaAtual += alturaDeUmaLinha + espacoExtraEntreLinhas;
		}

		return new Imagem(bitmap);
	}

	public void ativeModoDeSomaDeCores() {
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
	}

	public void desativeModoDeSomaDeCores() {
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	}

	public void garantaCapacidadeDeRetangulos(int capacidadeDeRetangulosDesejada) {
		if (getCapacidadeDeRetangulos() < capacidadeDeRetangulosDesejada) {
			setCapacidadeDeRetangulos(capacidadeDeRetangulosDesejada);

			if (getContagemDeRetangulos() > capacidadeDeRetangulosDesejada) {
				setContagemDeRetangulos(capacidadeDeRetangulosDesejada);
			}

			float[] vertices = getVertices();
			if (vertices == null) {
				setVertices(new float[FLOATS_POR_RETANGULO * capacidadeDeRetangulosDesejada]);
			} else {
				setVertices(Arrays.copyOf(vertices, FLOATS_POR_RETANGULO * capacidadeDeRetangulosDesejada));
			}
		}

		if (getCapacidadeDeRetangulosDoBuffer() < capacidadeDeRetangulosDesejada) {
			setCapacidadeDeRetangulosDoBuffer(capacidadeDeRetangulosDesejada);

			// Como nossos índices serão GL_UNSIGNED_SHORT, não podemos ter mais de 65536 vértices
			if (capacidadeDeRetangulosDesejada > 16384) {
				throw new RuntimeException("Não é possível desenhar mais de 16384 retângulos de uma vez");
			}

			// Quando a capacidade dos buffer não for suficiente, temos que recriar todos os buffers

			// Cada modelo possui quatro vértices, formando dois triângulos (por isso 6 índices)
			// Vértices:
			// 0   2
			//
			// 1   3
			//
			// Primeiro triângulo: 0 1 2
			// Segundo triângulo: 2 1 3
			int contagemDeIndices = (6 * capacidadeDeRetangulosDesejada) ;
			short[] indices = new short[contagemDeIndices];
			for (int i = 0, vertice = 0; i < contagemDeIndices; i += 6, vertice += 4) {
				// Primeiro triângulo
				indices[i] = (short)(vertice);
				indices[i + 1] = (short)(vertice + 1);
				indices[i + 2] = (short)(vertice + 2);

				// Segundo triângulo
				indices[i + 3] = (short)(vertice + 2);
				indices[i + 4] = (short)(vertice + 1);
				indices[i + 5] = (short)(vertice + 3);
			}

			ShortBuffer bufferDeIndices = ByteBuffer.allocateDirect(2 * contagemDeIndices).order(ByteOrder.nativeOrder()).asShortBuffer();

			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, getIdDoBufferDeIndices());
			// Cada índice tem 2 bytes (1 short = 2 bytes)
			GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * contagemDeIndices,
				bufferDeIndices.put(indices).position(0),
				GLES20.GL_STATIC_DRAW);

			setBufferDeVertices(ByteBuffer.allocateDirect(BYTES_POR_RETANGULO * capacidadeDeRetangulosDesejada).order(ByteOrder.nativeOrder()).asFloatBuffer());

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, getIdDoBufferDeVertices());
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BYTES_POR_RETANGULO * capacidadeDeRetangulosDesejada,
				getBufferDeVertices().put(getVertices()).position(0),
				GLES20.GL_DYNAMIC_DRAW);
		}
	}

	public void desenhe(Imagem imagem, float esquerda, float cima, float direita, float baixo, float alpha, float esquerdaTextura, float cimaTextura, float direitaTextura, float baixoTextura) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Apenas copia o ponto de entrada para o ponto destino:
		//
		// Ponto destino = Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		verticesDestino[indiceDoPrimeiroVertice     ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = esquerdaTextura;
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = cimaTextura;

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = esquerdaTextura;
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = baixoTextura;

		verticesDestino[indiceDoPrimeiroVertice + 10] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 11] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = direitaTextura;
		verticesDestino[indiceDoPrimeiroVertice + 14] = cimaTextura;

		verticesDestino[indiceDoPrimeiroVertice + 15] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 16] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = direitaTextura;
		verticesDestino[indiceDoPrimeiroVertice + 19] = baixoTextura;
	}

	public void desenhe(Imagem imagem, float esquerda, float cima, float direita, float baixo, float alpha, CoordenadasDeTextura coordenadasDeTextura) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Apenas copia o ponto de entrada para o ponto destino:
		//
		// Ponto destino = Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		verticesDestino[indiceDoPrimeiroVertice     ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 11] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 16] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}

	public void desenhe(Imagem imagem, float esquerda, float cima, float direita, float baixo, float alpha, CoordenadasDeTextura coordenadasDeTextura, float anguloEmRadianos, float destinoX, float destinoY) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Aplica manualmente uma matriz a todos os pontos, equivalente à seguinte operação:
		//
		// Ponto destino = Matriz de translação * Matriz de rotação * Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos);

		// O correto seria:
		// x destino = (cos * x) - (sen * y)
		// y destino = (sen * x) + (cos * y)
		// Porém, como no plano do bitmap, o valor de y cresce para baixo, e em OpenGL, ele cresce
		// para cima, invertemos o sinal do seno para compensar

		verticesDestino[indiceDoPrimeiroVertice     ] = (cos * esquerda) + (sen * cima) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = (cos * cima) - (sen * esquerda) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = (cos * esquerda) + (sen * baixo) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = (cos * baixo) - (sen * esquerda) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = (cos * direita) + (sen * cima) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 11] = (cos * cima) - (sen * direita) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = (cos * direita) + (sen * baixo) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 16] = (cos * baixo) - (sen * direita) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}

	public void desenhe(Imagem imagem, CoordenadasDeModelo coordenadasDeModelo, float alpha, CoordenadasDeTextura coordenadasDeTextura) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Apenas copia o ponto de entrada para o ponto destino:
		//
		// Ponto destino = Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		verticesDestino[indiceDoPrimeiroVertice     ] = coordenadasDeModelo.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = coordenadasDeModelo.getCima();
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = coordenadasDeModelo.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = coordenadasDeModelo.getBaixo();
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = coordenadasDeModelo.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 11] = coordenadasDeModelo.getCima();
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = coordenadasDeModelo.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 16] = coordenadasDeModelo.getBaixo();
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}

	public void desenhe(Imagem imagem, CoordenadasDeModelo coordenadasDeModelo, float alpha, CoordenadasDeTextura coordenadasDeTextura, float destinoX, float destinoY) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Aplica manualmente uma matriz a todos os pontos, equivalente à seguinte operação:
		//
		// Ponto destino = Matriz de translação * Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		float esquerda = coordenadasDeModelo.getEsquerda() + destinoX;
		float cima = coordenadasDeModelo.getCima() + destinoY;
		float direita = coordenadasDeModelo.getDireita() + destinoX;
		float baixo = coordenadasDeModelo.getBaixo() + destinoY;

		verticesDestino[indiceDoPrimeiroVertice     ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 11] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 16] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}

	public void desenhe(Imagem imagem, CoordenadasDeModelo coordenadasDeModelo, float alpha, CoordenadasDeTextura coordenadasDeTextura, float escalaX, float escalaY, float destinoX, float destinoY) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Aplica manualmente uma matriz a todos os pontos, equivalente à seguinte operação:
		//
		// Ponto destino = Matriz de translação * Matriz com fator de escala * Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		float esquerda = (coordenadasDeModelo.getEsquerda() * escalaX) + destinoX;
		float cima = (coordenadasDeModelo.getCima() * escalaY) + destinoY;
		float direita = (coordenadasDeModelo.getDireita() * escalaX) + destinoX;
		float baixo = (coordenadasDeModelo.getBaixo() * escalaY) + destinoY;

		verticesDestino[indiceDoPrimeiroVertice     ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = esquerda;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 11] = cima;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = direita;
		verticesDestino[indiceDoPrimeiroVertice + 16] = baixo;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}

	public void desenhe(Imagem imagem, CoordenadasDeModelo coordenadasDeModelo, float alpha, CoordenadasDeTextura coordenadasDeTextura, float anguloEmRadianos, float destinoX, float destinoY) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Aplica manualmente uma matriz a todos os pontos, equivalente à seguinte operação:
		//
		// Ponto destino = Matriz de translação * Matriz de rotação * Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos);

		float esquerda = coordenadasDeModelo.getEsquerda();
		float cima = coordenadasDeModelo.getCima();
		float direita = coordenadasDeModelo.getDireita();
		float baixo = coordenadasDeModelo.getBaixo();

		// O correto seria:
		// x destino = (cos * x) - (sen * y)
		// y destino = (sen * x) + (cos * y)
		// Porém, como no plano do bitmap, o valor de y cresce para baixo, e em OpenGL, ele cresce
		// para cima, invertemos o sinal do seno para compensar

		verticesDestino[indiceDoPrimeiroVertice     ] = (cos * esquerda) + (sen * cima) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = (cos * cima) - (sen * esquerda) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = (cos * esquerda) + (sen * baixo) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = (cos * baixo) - (sen * esquerda) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = (cos * direita) + (sen * cima) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 11] = (cos * cima) - (sen * direita) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = (cos * direita) + (sen * baixo) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 16] = (cos * baixo) - (sen * direita) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}

	public void desenhe(Imagem imagem, CoordenadasDeModelo coordenadasDeModelo, float alpha, CoordenadasDeTextura coordenadasDeTextura, float escalaX, float escalaY, float anguloEmRadianos, float destinoX, float destinoY) {
		// Quando a imagem é alterada, precisamos efetivamente desenhar todos os retângulos que
		// ficaram pendentes até o momento
		if (getImagemDosRetangulosPendentes() != imagem) {
			desenheRetangulosPendentes();
			setImagemDosRetangulosPendentes(imagem);
		}

		int indiceDoPrimeiroVertice = indiceDoProximoVerticeDosRetangulos();
		float[] verticesDestino = getVertices();

		// Aplica manualmente uma matriz a todos os pontos, equivalente à seguinte operação:
		//
		// Ponto destino = Matriz de translação * Matriz de rotação * Matriz com fator de escala * Ponto de entrada
		//
		// Ordem dos pontos em verticesDestino:
		// x do ponto 0 (esquerda)
		// y do ponto 0 (cima)
		// x do ponto 1 (esquerda)
		// y do ponto 1 (baixo)
		// x do ponto 2 (direita)
		// y do ponto 2 (cima)
		// x do ponto 3 (direita)
		// y do ponto 3 (baixo)

		float cos = (float)Math.cos(anguloEmRadianos);
		float sen = (float)Math.sin(anguloEmRadianos);

		float esquerda = coordenadasDeModelo.getEsquerda() * escalaX;
		float cima = coordenadasDeModelo.getCima() * escalaY;
		float direita = coordenadasDeModelo.getDireita() * escalaX;
		float baixo = coordenadasDeModelo.getBaixo() * escalaY;

		// O correto seria:
		// x destino = (cos * x) - (sen * y)
		// y destino = (sen * x) + (cos * y)
		// Porém, como no plano do bitmap, o valor de y cresce para baixo, e em OpenGL, ele cresce
		// para cima, invertemos o sinal do seno para compensar

		verticesDestino[indiceDoPrimeiroVertice     ] = (cos * esquerda) + (sen * cima) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 1 ] = (cos * cima) - (sen * esquerda) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 2 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 3 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 4 ] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 5 ] = (cos * esquerda) + (sen * baixo) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 6 ] = (cos * baixo) - (sen * esquerda) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 7 ] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 8 ] = coordenadasDeTextura.getEsquerda();
		verticesDestino[indiceDoPrimeiroVertice + 9 ] = coordenadasDeTextura.getBaixo();

		verticesDestino[indiceDoPrimeiroVertice + 10] = (cos * direita) + (sen * cima) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 11] = (cos * cima) - (sen * direita) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 12] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 13] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 14] = coordenadasDeTextura.getCima();

		verticesDestino[indiceDoPrimeiroVertice + 15] = (cos * direita) + (sen * baixo) + destinoX;
		verticesDestino[indiceDoPrimeiroVertice + 16] = (cos * baixo) - (sen * direita) + destinoY;
		verticesDestino[indiceDoPrimeiroVertice + 17] = alpha;
		verticesDestino[indiceDoPrimeiroVertice + 18] = coordenadasDeTextura.getDireita();
		verticesDestino[indiceDoPrimeiroVertice + 19] = coordenadasDeTextura.getBaixo();
	}
}
