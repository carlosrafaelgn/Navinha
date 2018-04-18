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
package br.com.carlosrafaelgn.navinha.jogo.android;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.Arrays;
import java.util.Comparator;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import br.com.carlosrafaelgn.navinha.jogo.cenarios.CenarioInicial;
import br.com.carlosrafaelgn.navinha.jogo.cenarios.CenarioTeste;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.interacao.Ponteiro;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.Vetor;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.sincronizacao.MutexSimples;

public class MainActivity extends Activity implements Jogo.Agendador, Jogo.Observador {
	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static final class ObservadorDaInterfaceDeSistema implements View.OnSystemUiVisibilityChangeListener {
		//------------------------------------------------------------------------------------------
		// Constantes
		//------------------------------------------------------------------------------------------

		private static final int MENSAGEM_ESCONDA_INTERFACE_DO_SISTEMA = 0x0100;

		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private View decor;
		private Handler tratadorDaThreadPrincipal;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ObservadorDaInterfaceDeSistema(View decor) {
			setDecor(decor);

			// Só precisamos mesmo do tratador, porque o ato de esconder a interface do sistema deve
			// ocorrer um certo tempo depois dela ter sido exibida, caso contrário, o jogador não
			// teria tempo de utilizá-la
			setTratadorDaThreadPrincipal(new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					if (msg.what == MENSAGEM_ESCONDA_INTERFACE_DO_SISTEMA) {
						escondaInterfaceDoSistema();
					}
					return true;
				}
			}));
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private View getDecor() {
			return decor;
		}

		private void setDecor(View decor) {
			this.decor = decor;
		}

		private Handler getTratadorDaThreadPrincipal() {
			return tratadorDaThreadPrincipal;
		}

		private void setTratadorDaThreadPrincipal(Handler tratadorDaThreadPrincipal) {
			this.tratadorDaThreadPrincipal = tratadorDaThreadPrincipal;
		}

		//------------------------------------------------------------------------------------------
		// Métodos privados e protegidos
		//------------------------------------------------------------------------------------------

		@SuppressLint("InlinedApi")
		private void escondaInterfaceDoSistema() {
			View decor = getDecor();

			if (decor == null) {
				return;
			}

			// Configura a interface do sistema Android, para que ela fique invisível, e para que
			// nossa aplicação ocupe toda a área disponível da tela
			// Contudo, perceba que o comportamento é diferente para diferentes versões do Android:
			// - 19 ou superior: esconde toda a interface do sistema
			// - 18 ou inferior: apenas deixa a interface do sistema em um modo que não irá
			// incomodar muito o jogador, mas não esconde a interface (nessas versões, se for pedido
			// para esconder a interface, qualquer clique/toque fará com que ela seja exibida
			// novamente, e não há nada que possamos fazer para prevenir isso)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
					View.SYSTEM_UI_FLAG_FULLSCREEN |
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
					View.SYSTEM_UI_FLAG_LOW_PROFILE |
					View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
					View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			} else {
				decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
					View.SYSTEM_UI_FLAG_FULLSCREEN |
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
					View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void onSystemUiVisibilityChange(int visibility) {
			// onSystemUiVisibilityChange é executado toda vez que a visibilidade da interface do
			// sistema é alterada, seja para visível, como para invisível

			if (getDecor() == null) {
				return;
			}

			// Verifica se a interface do sistema acabou de ficar visível
			if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
				// Esconde a interface do sistema três segundos depois dela ter sido exibida
				getTratadorDaThreadPrincipal().sendEmptyMessageDelayed(MENSAGEM_ESCONDA_INTERFACE_DO_SISTEMA, 3000);
			}
		}

		public void prepare() {
			View decor = getDecor();

			if (decor == null) {
				return;
			}

			escondaInterfaceDoSistema();

			// Pede para sermos avisados toda vez que a visibilidade da interface do sistema for
			// alterada
			decor.setOnSystemUiVisibilityChangeListener(this);
		}

		public void destrua() {
			View decor = getDecor();

			if (decor == null) {
				return;
			}

			// Não nos interessa mais receber nenhum tipo de aviso sobre a visibilidade do sistema
			decor.setOnSystemUiVisibilityChangeListener(null);

			setDecor(null);
			setTratadorDaThreadPrincipal(null);
		}
	}

	private final class GLView extends GLSurfaceView implements GLSurfaceView.EGLContextFactory, GLSurfaceView.EGLWindowSurfaceFactory, GLSurfaceView.Renderer {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private EGLConfig config;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public GLView(Context context) {
			super(context);
			setEGLContextFactory(this);
			setEGLWindowSurfaceFactory(this);
			setRenderer(this);
			setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				setPreserveEGLContextOnPause(false);
			}
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private EGLConfig getConfig() {
			return config;
		}

		private void setConfig(EGLConfig config) {
			this.config = config;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig config) {
			// Documentação oficial das funções utilizadas aqui:
			// https://www.khronos.org/registry/egl/sdk/docs/man/html/eglChooseConfig.xhtml
			// https://www.khronos.org/registry/egl/sdk/docs/man/html/eglGetConfigAttrib.xhtml
			// https://www.khronos.org/registry/egl/sdk/docs/man/html/eglCreateContext.xhtml

			// Para criar um contexto do OpenGL ES, é necessário especificar qual configuração será
			// utilizada
			// Contudo, apesar de cada configuração possuir diversos atributos, não é possível
			// simplesmente fornecer os valores desejados para cada atributo, e criarmos "nossa
			// configuração amada"
			// Cada sistema possui um conjunto limitado e específico de configurações já prontas,
			// restando a nós escolher qual das configurações disponíveis melhor se adapta ao nosso
			// desejo
			// *** Repare que os critérios utilizados para escolher uma configuração aqui estão
			// levando em conta as necessidades desse jogo!
			// Outros jogos podem possuir necessidades diferentes!

			egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			setConfig(null);

			// EGL_FALSE = 0
			// EGL_TRUE = 1
			final EGLConfig[] configuracoes = new EGLConfig[64], configuracoesSelecionadas = new EGLConfig[64];
			final int[] num_config = { 0 }, value = new int[1];
			final int[] none = { EGL10.EGL_NONE };
			// EGL_CONTEXT_CLIENT_VERSION = 0x3098
			final int[] v2 = { 0x3098, 2, EGL10.EGL_NONE };

			int contagemSelecionadas = 0;

			if (egl.eglGetConfigs(display, configuracoes, 64, num_config) && num_config[0] > 0) {

				// Depois de obter as configurações suportadas pelo dispositivo, vamos fazer uma
				// triagem inicial para eliminar todas as configurações que não são aceitáveis

				for (int i = 0; i < num_config[0]; i++) {
					// Primeira configuração obrigatória: EGL_RENDERABLE_TYPE deve possuir o bit
					// EGL_OPENGL_ES2_BIT, indicando suporte a OpenGL ES 2
					egl.eglGetConfigAttrib(display, configuracoes[i], EGL10.EGL_RENDERABLE_TYPE, value);
					//EGL_OPENGL_ES2_BIT = 4
					if ((value[0] & 4) == 0) continue;

					// Segunda configuração obrigatória: EGL_SURFACE_TYPE deve possuir o bit
					// EGL_WINDOW_BIT, indicando que a configuração pode ser utilizada em janelas, e
					// não apenas na memória do dispositivo
					egl.eglGetConfigAttrib(display, configuracoes[i], EGL10.EGL_SURFACE_TYPE, value);
					if ((value[0] & EGL10.EGL_WINDOW_BIT) == 0) continue;

					// Terceira configuração obrigatória: a quantidade de bits por canal (R, G e B)
					// dos pixels deve ser pelo menos 4 (na prática, normalmente teremos 555, 565 ou
					// 888)
					egl.eglGetConfigAttrib(display, configuracoes[i], EGL10.EGL_RED_SIZE, value);
					if (value[0] < 4) continue;
					egl.eglGetConfigAttrib(display, configuracoes[i], EGL10.EGL_GREEN_SIZE, value);
					if (value[0] < 4) continue;
					egl.eglGetConfigAttrib(display, configuracoes[i], EGL10.EGL_BLUE_SIZE, value);
					if (value[0] < 4) continue;

					// Essa configuração possui os requisitos mínimos necessários
					configuracoesSelecionadas[contagemSelecionadas++] = configuracoes[i];
				}
			}

			// Se nenhuma configuração pareceu possuir nossos requisitos mínimos, vamos criar o
			// contexto com a configuração que nos foi enviada, e pronto!
			if (contagemSelecionadas == 0) {
				return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, none);
			}

			// Ordena as configurações selecionadas, de modo que ao final, as configurações estejam
			// armazenadas por ordem de preferência, da mais preferida, para a menos preferida
			Arrays.sort(configuracoesSelecionadas, 0, contagemSelecionadas, new Comparator<EGLConfig>() {
				@Override
				public int compare(EGLConfig a, EGLConfig b) {
					int x;

					// Damos preferência a buffers do tipo RGB
					egl.eglGetConfigAttrib(display, a, EGL10.EGL_COLOR_BUFFER_TYPE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_COLOR_BUFFER_TYPE, value);
					if (x != value[0]) {
						return (x == EGL10.EGL_RGB_BUFFER) ? -1 : 1;
					}

					// Em seguida, preferimos as configurações com suporte a renderização nativa
					egl.eglGetConfigAttrib(display, a, EGL10.EGL_NATIVE_RENDERABLE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_NATIVE_RENDERABLE, value);
					if (x != value[0]) {
						return (x != 0) ? -1 : 1;
					}

					// Apesar de RGB 888 produzir imagens com mais cores, como nosso jogo não possui
					// muitos efeitos, e é essencialmente preto, com alguns textos e poucos sprites,
					// RGB 565 gastará menos memória
					// Mas isso, visto que EGL_NATIVE_RENDERABLE foi igual em ambas as configurações
					// (se existisse uma configuração nativa com 888, e outra não-nativa com 565,
					// iríamos preferir a 888 nativa)
					egl.eglGetConfigAttrib(display, a, EGL10.EGL_RED_SIZE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_RED_SIZE, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					// Daqui em diante, vamos dar preferência às configurações que utilizam menos
					// recursos (quanto menor, melhor) em ordem de importância

					// Vamos analisar a presença do bit EGL_SWAP_BEHAVIOR_PRESERVED_BIT, e dar
					// preferência a uma configuração sem esse bit (a configuração sem o bit valerá
					// 0, enquanto que configurações com esse bit valem 0x0400, mantendo a ideia de
					// que quanto menor, melhor)
					// EGL_SWAP_BEHAVIOR_PRESERVED_BIT = 0x0400
					egl.eglGetConfigAttrib(display, a, EGL10.EGL_SURFACE_TYPE, value);
					x = value[0] & 0x0400;
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_SURFACE_TYPE, value);
					value[0] &= 0x0400;
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_SAMPLE_BUFFERS, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_SAMPLE_BUFFERS, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_SAMPLES, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_SAMPLES, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_BUFFER_SIZE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_BUFFER_SIZE, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_DEPTH_SIZE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_DEPTH_SIZE, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_STENCIL_SIZE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_STENCIL_SIZE, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_ALPHA_MASK_SIZE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_ALPHA_MASK_SIZE, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					egl.eglGetConfigAttrib(display, a, EGL10.EGL_ALPHA_SIZE, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_ALPHA_SIZE, value);
					if (x != value[0]) {
						return (x - value[0]);
					}

					// Critério final (apenas para desempatar, caso chegue até aqui)
					egl.eglGetConfigAttrib(display, a, EGL10.EGL_CONFIG_ID, value);
					x = value[0];
					egl.eglGetConfigAttrib(display, b, EGL10.EGL_CONFIG_ID, value);
					return (x - value[0]);
				}
			});

			// De acordo com o código fonte do Android:
			// http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/opengl/GLSurfaceView.java#941
			// o parâmetro nativeWindow do método createWindowSurface() é esse surfaceHolder
			SurfaceHolder surfaceHolder = getHolder();
			for (int i = 0; i < contagemSelecionadas; i++) {
				// Agora que as configurações estão em ordem de preferência, vamos a um último
				// filtro, que é a quantidade de bits por pixel (apenas por segurança)
				// Queremos apenas configurações 888 ou 565 (o valor do alpha não nos interessa)
				final int r, g, b;
				egl.eglGetConfigAttrib(display, configuracoesSelecionadas[i], EGL10.EGL_RED_SIZE, value);
				r = value[0];
				egl.eglGetConfigAttrib(display, configuracoesSelecionadas[i], EGL10.EGL_GREEN_SIZE, value);
				g = value[0];
				egl.eglGetConfigAttrib(display, configuracoesSelecionadas[i], EGL10.EGL_BLUE_SIZE, value);
				b = value[0];
				if ((r != 8 || g != 8 || b != 8) &&
					(r != 5 || g != 6 || b != 5)) {
					continue;
				}

				EGLSurface surface = null;
				try {
					// Antes de retornar do método, vamos tentar efetivamente criar os objetos com
					// essa configuração, e caso algo saia errado, tentamos a próxima configuração
					setConfig(configuracoesSelecionadas[i]);

					EGLContext contextoDeTeste = egl.eglCreateContext(display, getConfig(), EGL10.EGL_NO_CONTEXT, v2);
					if (contextoDeTeste == null || contextoDeTeste == EGL10.EGL_NO_CONTEXT) {
						contextoDeTeste = egl.eglCreateContext(display, getConfig(), EGL10.EGL_NO_CONTEXT, none);
					}
					if (contextoDeTeste != null && contextoDeTeste != EGL10.EGL_NO_CONTEXT) {
						// Um último teste antes de prosseguirmos: será que conseguimos criar uma
						// surface com nossa configuração, e conseguimos atrelar essa surface ao
						// nosso contexto criado?

						surfaceHolder.setFormat((r == 5) ? PixelFormat.RGB_565 : PixelFormat.RGBA_8888);
						surface = egl.eglCreateWindowSurface(display, getConfig(), surfaceHolder, null);

						if (surface != null && surface != EGL10.EGL_NO_SURFACE) {
							// Vamos tentar aplicar o contexto criado à superfície da janela
							if (egl.eglMakeCurrent(display, surface, surface, contextoDeTeste)) {
								// Sucesso! Tudo deu certo!
								return contextoDeTeste;
							}
						}

						// Destrói o contexto que foi criado, porque vamos ter que tentar de novo...
						egl.eglDestroyContext(display, contextoDeTeste);

						setConfig(null);
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				} finally {
					// Ao final, limpamos tudo (dando certo ou dando errado)
					egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
					if (surface != null && surface != EGL10.EGL_NO_SURFACE) {
						egl.eglDestroySurface(display, surface);
					}
				}
			}

			// As coisas realmente não foram conforme o planejado... então, vamos prosseguir de
			// qualquer jeito, criando o contexto com a configuração que nos foi enviada, e pronto!
			setConfig(null);
			return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, none);
		}

		@Override
		public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
			Jogo jogo = Jogo.getJogo();

			try {
				getControleDoJogo().entre1();

				// Como o jogo perdeu a interatividade com o jogador, e o contexto do OpenGL está
				// para ser destruído, já aproveitamos para liberar a memória e os recursos usados
				jogo.interatividadeComJogadorPerdida();

			} finally {
				getControleDoJogo().saia1();
			}

			if (egl != null && display != null && context != null) {
				egl.eglDestroyContext(display, context);
			}
		}

		@Override
		public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
			try {
				return egl.eglCreateWindowSurface(display, (getConfig() != null) ? getConfig() : config, nativeWindow, null);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			return null;
		}

		@Override
		public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
			if (egl != null && display != null && surface != null) {
				egl.eglDestroySurface(display, surface);
			}
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// Vamos esperar pela primeira execução do método onSurfaceChanged(), já que, de acordo
			// com a documentação, ele sempre é executado ao menos uma vez depois de
			// onSurfaceCreated()
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Jogo jogo = Jogo.getJogo();

			try {
				getControleDoJogo().entre1();

				jogo.interatividadeComJogadorRecuperada(width, height);

			} finally {
				getControleDoJogo().saia1();
			}
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			Jogo jogo = Jogo.getJogo();

			try {
				getControleDoJogo().entre1();

				jogo.processeEDesenheUmQuadro();

			} finally {
				getControleDoJogo().saia1();
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// Utilizaremos os fatores X e Y da tela para converter x e y da tela para vista
			Tela tela = Tela.getTela();
			float fatorX = tela.getFatorTelaVistaX();
			float fatorY = tela.getFatorTelaVistaY();

			Vetor<Ponteiro> ponteiros = Jogo.getJogo().getPonteirosThreadPrincipal();

			int indice, id;

			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				indice = event.getActionIndex();
				id = event.getPointerId(indice);
				if (id < ponteiros.comprimento()) {
					ponteiros.item(id).atualize(fatorX * event.getX(indice), fatorY * event.getY(indice), true);
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				indice = event.getActionIndex();
				id = event.getPointerId(indice);
				if (id < ponteiros.comprimento()) {
					ponteiros.item(id).atualize(fatorX * event.getX(indice), fatorY * event.getY(indice), false);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				for (int i = event.getPointerCount() - 1; i >= 0; i--) {
					id = event.getPointerId(i);
					if (id < ponteiros.comprimento()) {
						ponteiros.item(id).atualize(fatorX * event.getX(i), fatorY * event.getY(i), true);
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				// Define todos os ponteiros como não pressionados, e em uma posição fora da vista
				for (id = ponteiros.comprimento() - 1; id >= 0; id--) {
					ponteiros.item(id).atualize(-1.0f, -1.0f, false);
				}
				break;
			}

			return true;
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private MutexSimples controleDoJogo;
	private GLView glView;
	private ObservadorDaInterfaceDeSistema observadorDaInterfaceDeSistema;

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private MutexSimples getControleDoJogo() {
		return controleDoJogo;
	}

	private void setControleDoJogo(MutexSimples controleDoJogo) {
		this.controleDoJogo = controleDoJogo;
	}

	private GLView getGlView() {
		return glView;
	}

	private void setGlView(GLView glView) {
		this.glView = glView;
	}

	private ObservadorDaInterfaceDeSistema getObservadorDaInterfaceDeSistema() {
		return observadorDaInterfaceDeSistema;
	}

	private void setObservadorDaInterfaceDeSistema(ObservadorDaInterfaceDeSistema observadorDaInterfaceDeSistema) {
		this.observadorDaInterfaceDeSistema = observadorDaInterfaceDeSistema;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void prepareObservadorDaInterfaceDoSistema() {
		ObservadorDaInterfaceDeSistema observadorDaInterfaceDeSistema = getObservadorDaInterfaceDeSistema();
		if (observadorDaInterfaceDeSistema == null) {
			observadorDaInterfaceDeSistema = new ObservadorDaInterfaceDeSistema(getWindow().getDecorView());
			setObservadorDaInterfaceDeSistema(observadorDaInterfaceDeSistema);
		}
		observadorDaInterfaceDeSistema.prepare();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void destruaObservadorDaInterfaceDoSistema() {
		ObservadorDaInterfaceDeSistema observadorDaInterfaceDeSistema = getObservadorDaInterfaceDeSistema();
		if (observadorDaInterfaceDeSistema != null) {
			observadorDaInterfaceDeSistema.destrua();
			setObservadorDaInterfaceDeSistema(null);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(
			// Não queremos que a tela apague mesmo se o jogador ficar parado por algum tempo
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		ViewGroup viewGroup = (ViewGroup)findViewById(android.R.id.content);

		setControleDoJogo(new MutexSimples());

		// Configura o objeto do jogo indicando qual será o cenário inicial
		Jogo.getJogo().prepare(getApplication(), this, this, new CenarioInicial());

		// Configura o controle que controlará o Play Games
		ControleDoPlayGames.getControleDoPlayGames().prepare(this, viewGroup);

		// Cria a view responsável por exibir o jogo
		GLView glView = new GLView(getApplication());
		setGlView(glView);
		viewGroup.addView(glView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onPause() {
		super.onPause();

		Jogo jogo = Jogo.getJogo();

		try {
			getControleDoJogo().entre0();

			// É necessário garantir que o jogo esteja realmente parado
			jogo.pauseOProcessamento();

		} finally {
			getControleDoJogo().saia0();
		}

		// A documentação da classe GLSurfaceView pede que isso seja feito
		getGlView().onPause();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// Só destrói o observador da interface do sistema, caso estejamos em um Android com
			// versão 14 ou superior (caso contrário, ele não foi criado no método onResume())
			destruaObservadorDaInterfaceDoSistema();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// Podemos controlar a visibilidade da interface do sistema em Androids com versão 14 ou
			// superior (no caso do nosso jogo, queremos que a interface desapareça por completo)
			prepareObservadorDaInterfaceDoSistema();
		}

		// Não vamos retomar o jogo aqui pois precisamos esperar pela criação da surface

		// A documentação da classe GLSurfaceView pede que isso seja feito
		getGlView().onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Jogo jogo = Jogo.getJogo();

		try {
			getControleDoJogo().entre0();

			// É isso! Não há nada mais o que ser feito!
			jogo.encerre();

		} finally {
			getControleDoJogo().saia0();
		}

		// A view não será mais necessária
		setGlView(null);

		// Destrói o controle do Play Games
		ControleDoPlayGames.getControleDoPlayGames().destrua();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Alerta o controle do Play Games sobre o término de uma activity, pois, provavelmente,
		// ele foi quem iniciou a activity
		ControleDoPlayGames controleDoPlayGames = ControleDoPlayGames.getControleDoPlayGames();
		if (controleDoPlayGames != null) {
			controleDoPlayGames.onActivityResult(requestCode, resultCode);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void onBackPressed() {
		Jogo.getJogo().botaoVoltarPressionado();
	}

	@Override
	public void agendeParaOProximoQuadro(Runnable runnable) {
		// Tenta agendar esse runnable para executar na própria thread do jogo, no próximo quadro
		GLView glView = getGlView();
		if (glView != null) {
			glView.queueEvent(runnable);
		}
	}

	@Override
	public void jogoEncerrado() {
		finish();
	}
}
