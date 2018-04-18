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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.os.Build;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public final class Imagem extends Recurso {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private int id, idDoDrawable;
	// Armazena a largura e a altura como float para auxiliar o OpenGL
	private float largura, altura;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Imagem(int idDoDrawable) {
		incorporeBitmap(idDoDrawable);
	}

	public Imagem(Bitmap bitmap) {
		incorporeBitmap(bitmap);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	private int getIdDoDrawable() {
		return idDoDrawable;
	}

	private void setIdDoDrawable(int idDoDrawable) {
		this.idDoDrawable = idDoDrawable;
	}

	public float getLargura() {
		return largura;
	}

	private void setLargura(float largura) {
		this.largura = largura;
	}

	public float getAltura() {
		return altura;
	}

	private void setAltura(float altura) {
		this.altura = altura;
	}

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getId() != 0);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void carregueInternamente() {
		int idDoDrawable = getIdDoDrawable();
		if (idDoDrawable != 0) {
			incorporeBitmap(idDoDrawable);
		}
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		Tela.getTela().destruaUmaTextura(getId());
		setId(0);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setIdDoDrawable(0);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void incorporeBitmap(int idDoDrawable) {
		BitmapFactory.Options opcoes = new BitmapFactory.Options();
		opcoes.inDither = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			opcoes.inMutable = false;
		}
		opcoes.inPreferQualityOverSpeed = true;
		opcoes.inPreferredConfig = Bitmap.Config.ARGB_8888;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			opcoes.inPremultiplied = false;
		}
		incorporeBitmap(BitmapFactory.decodeResource(Jogo.getJogo().getContext().getResources(), idDoDrawable, opcoes));

		setIdDoDrawable(idDoDrawable);
	}

	public void incorporeBitmap(Bitmap bitmap) {
		// Caso incorporeBitmap() seja chamado sem que antes a imagem anterior tenha sido liberada
		libere();

		int largura = bitmap.getWidth();
		int altura = bitmap.getHeight();

		int[] tamanhoMaximo = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, tamanhoMaximo, 0);
		if (largura > tamanhoMaximo[0] || altura > tamanhoMaximo[0]) {
			throw new IllegalArgumentException("As dimensões do bitmap (" + largura + "x" + altura + ") excedem os limites do OpenGL (" + tamanhoMaximo[0] + "x" + tamanhoMaximo[0] + ")");
		}

		setLargura(largura);
		setAltura(altura);

		// Pede para o OpenGL criar uma textura cujo id será armazenado em id

		// Limpa as marcações de erro do OpenGL
		while (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			GLES20.glGetError();
		}

		setId(Tela.getTela().crieUmaTextura());

		// Torna a textura recém criada como a textura atual do OpenGL
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getId());

		// Configura o modo de redimensionamento da textura (para esse jogo, queremos o mínimo de
		// suavização, por isso utilizamos GL_NEAREST, caso contrário, utilizaríamos GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		// Configura o que fazer caso seja pedido uma coordenada fora da área da textura
		// (GL_CLAMP_TO_EDGE faz com que esses casos retornem uma cor 100% transparente / vazia)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		// Esse processo de carregar o bitmap para a memória de vídeo é meio extenso, mas é
		// necessário, por uma série de motivos:
		// - GLUtils.texImage2D() não funciona bem para bitmaps com alpha
		// - Processadores ao redor do mundo podem trabalhar com grupos de bytes em ordens
		// diferentes, mas o OpenGL sempre precisará dos bytes nessa ordem (de 0 a 3): R, G, B, A
		// - O método Bitmap.getPixels() devolve os bytes nessa ordem (de 0 a 3): B, G, R, A

		// Carrega os pixels do bitmap para uma memória temporária
		int[] pixels = new int[largura * altura];
		bitmap.getPixels(pixels, 0, largura, 0, 0, largura, altura);
		// Libera o bitmap, já que não será mais necessário
		bitmap.recycle();

		// Armazena os pixels em um buffer, conforme o OpenGL precisa
		IntBuffer bufferDePixels = IntBuffer.wrap(pixels);

		// Vamos garantir que os bytes chegarão ao OpenGL na ordem correta, por isso vamos extrair
		// cada inteiro para dentro dos bytes
		if (bufferDePixels.order() == ByteOrder.LITTLE_ENDIAN) {
			for (int i = pixels.length - 1; i >= 0; i--) {
				int cor = pixels[i];
				// Converte de B, G, R, A para R, G, B, A
				pixels[i] = (cor & 0xff00ff00) | ((cor & 0xff) << 16) | ((cor >>> 16) & 0xff);
			}
		} else {
			for (int i = pixels.length - 1; i >= 0; i--) {
				int cor = pixels[i];
				// Converte de B, G, R, A para R, G, B, A
				cor = (cor & 0xff00ff00) | ((cor & 0xff) << 16) | ((cor >>> 16) & 0xff);
				// Agora inverte a ordem dos bytes (poderíamos melhorar o desempenho aqui, juntando
				// ambas etapas...)
				pixels[i] = (((cor & 0xff000000) >>> 24) |
					((cor & 0x00ff0000) >>>  8) |
					((cor & 0x0000ff00) <<  8) |
					((cor & 0x000000ff) << 24));
			}
		}

		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, largura, altura, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bufferDePixels);

		if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("Erro ao carregar a textura");
		}

		// Não precisa deixar a textura como sendo a textura atual caso não vá mais utilizar
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
}
