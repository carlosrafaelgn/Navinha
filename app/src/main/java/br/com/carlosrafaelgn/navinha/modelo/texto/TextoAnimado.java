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

import br.com.carlosrafaelgn.navinha.modelo.animacao.contadores.Contador;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.Interpolador;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDePontos;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Ponto;

public final class TextoAnimado extends Texto {
	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	public interface Observador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void animacaoDoTextoTerminada(TextoAnimado textoAnimado);
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private InterpoladorDePontos interpoladorDePontos;
	private Interpolador interpoladorDoAlpha, interpoladorDaEscalaX, interpoladorDaEscalaY, interpoladorDoAngulo;
	private float duracaoDaAnimacao;
	private boolean observadorAvisado, animacaoInvertida;
	private Observador observador;
	private Contador contador;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public TextoAnimado(Alfabeto alfabeto, String texto, float espacamentoEntreLinhas, int alinhamentoDoPivo, float duracaoDaAnimacao, InterpoladorDePontos interpoladorDePontos) {
		this(alfabeto, texto, espacamentoEntreLinhas, alinhamentoDoPivo, duracaoDaAnimacao, Contador.UMA_VEZ, interpoladorDePontos);
	}

	public TextoAnimado(Alfabeto alfabeto, String texto, float espacamentoEntreLinhas, int alinhamentoDoPivo, float duracaoDaAnimacao, int tipoDoContador, InterpoladorDePontos interpoladorDePontos) {
		super(alfabeto, texto, espacamentoEntreLinhas, alinhamentoDoPivo);

		setAlinhamentoDoPivo(alinhamentoDoPivo);

		setInterpoladorDePontos(interpoladorDePontos);

		reinicieAnimacao(duracaoDaAnimacao, tipoDoContador);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public InterpoladorDePontos getInterpoladorDePontos() {
		return interpoladorDePontos;
	}

	public void setInterpoladorDePontos(InterpoladorDePontos interpoladorDePontos) {
		this.interpoladorDePontos = interpoladorDePontos;
	}

	public float getX() {
		return getInterpoladorDePontos().getUltimoPontoInterpolado().getX();
	}

	public float getY() {
		return getInterpoladorDePontos().getUltimoPontoInterpolado().getY();
	}

	public Interpolador getInterpoladorDoAlpha() {
		return interpoladorDoAlpha;
	}

	public void setInterpoladorDoAlpha(Interpolador interpoladorDoAlpha) {
		this.interpoladorDoAlpha = interpoladorDoAlpha;
	}

	public Interpolador getInterpoladorDaEscalaX() {
		return interpoladorDaEscalaX;
	}

	public void setInterpoladorDaEscalaX(Interpolador interpoladorDaEscalaX) {
		this.interpoladorDaEscalaX = interpoladorDaEscalaX;
	}

	public Interpolador getInterpoladorDaEscalaY() {
		return interpoladorDaEscalaY;
	}

	public void setInterpoladorDaEscalaY(Interpolador interpoladorDaEscalaY) {
		this.interpoladorDaEscalaY = interpoladorDaEscalaY;
	}

	public Interpolador getInterpoladorDoAngulo() {
		return interpoladorDoAngulo;
	}

	public void setInterpoladorDoAngulo(Interpolador interpoladorDoAngulo) {
		this.interpoladorDoAngulo = interpoladorDoAngulo;
	}

	public float getDuracaoDaAnimacao() {
		return duracaoDaAnimacao;
	}

	private void setDuracaoDaAnimacao(float duracaoDaAnimacao) {
		if (duracaoDaAnimacao <= 0.0f) {
			throw new IllegalArgumentException("duracaoDaAnimacao deve ser > 0");
		}

		this.duracaoDaAnimacao = duracaoDaAnimacao;
	}

	private boolean isObservadorAvisado() {
		return observadorAvisado;
	}

	private void setObservadorAvisado(boolean observadorAvisado) {
		this.observadorAvisado = observadorAvisado;
	}

	public boolean isAnimacaoInvertida() {
		return animacaoInvertida;
	}

	public void setAnimacaoInvertida(boolean animacaoInvertida) {
		this.animacaoInvertida = animacaoInvertida;
	}

	public boolean isAnimacaoTerminada() {
		return (getContador().getValor() >= 1.0f);
	}

	public Observador getObservador() {
		return observador;
	}

	public void setObservador(Observador observador) {
		this.observador = observador;
	}

	private Contador getContador() {
		return contador;
	}

	private void setContador(Contador contador) {
		this.contador = contador;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setInterpoladorDePontos(null);
		setInterpoladorDoAlpha(null);
		setInterpoladorDaEscalaX(null);
		setInterpoladorDaEscalaY(null);
		setInterpoladorDoAngulo(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe Texto, para permitir
		// que ela destrua seus recursos
		super.destruaInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void reinicieAnimacao(float duracaoDaAnimacao) {
		reinicieAnimacao(duracaoDaAnimacao, Contador.UMA_VEZ);
	}

	public void reinicieAnimacao(float duracaoDaAnimacao, int tipoDoContador) {
		setDuracaoDaAnimacao(duracaoDaAnimacao);
		setObservadorAvisado(false);
		// Repare que para isAnimacaoTerminada() funcionar corretamente *todas as vezes*, o contador
		// deve ser um ContadorUmaVez
		setContador(Contador.criePorTipo(tipoDoContador, 0.0f, 1.0f, 0.0f, 1.0f / duracaoDaAnimacao));
	}

	@Override
	public void processeUmQuadro(float deltaSegundos) {
		Contador contador = getContador();
		contador.conte(deltaSegundos);

		if (!isObservadorAvisado() && isAnimacaoTerminada()) {
			// Avisa, apenas uma vez, quem estava nos observando
			setObservadorAvisado(true);

			Observador observador = getObservador();
			if (observador != null) {
				observador.animacaoDoTextoTerminada(this);
			}
		}
	}

	@Override
	public void desenheUmQuadro() {
		// Calcula o intervalo, de modo que ele varie de 0 a 1 ao longo do tempo da animação
		float intervalo = getContador().getValor();

		if (isAnimacaoInvertida()) {
			// Se a animação for invertida, inverte o intervalo (ele passa a variar de 1 a 0)
			intervalo = 1.0f - intervalo;
		}

		// Pede para o interpolador calcular a posição do texto com base no intervalo atual
		Ponto ponto = getInterpoladorDePontos().interpole(intervalo);

		float alpha;
		Interpolador interpoladorDoAlpha = getInterpoladorDoAlpha();
		if (interpoladorDoAlpha != null) {
			alpha = interpoladorDoAlpha.interpole(intervalo);
		} else {
			alpha = 1.0f;
		}

		Interpolador interpoladorDaEscalaX = getInterpoladorDaEscalaX();
		Interpolador interpoladorDaEscalaY = getInterpoladorDaEscalaY();
		Interpolador interpoladorDoAngulo = getInterpoladorDoAngulo();

		// Vamos tentar encontrar o método de desenho mais eficiente para o caso atual
		if (interpoladorDoAngulo == null) {
			if (interpoladorDaEscalaX == null && interpoladorDaEscalaY == null) {
				desenhe(alpha,
					ponto.getX(),
					ponto.getY());
			} else if (interpoladorDaEscalaY == null) {
				desenhe(alpha,
					interpoladorDaEscalaX.interpole(intervalo),
					1.0f,
					ponto.getX(),
					ponto.getY());
			} else if (interpoladorDaEscalaX == null) {
				desenhe(alpha,
					1.0f,
					interpoladorDaEscalaY.interpole(intervalo),
					ponto.getX(),
					ponto.getY());
			} else {
				desenhe(alpha,
					interpoladorDaEscalaX.interpole(intervalo),
					interpoladorDaEscalaY.interpole(intervalo),
					ponto.getX(),
					ponto.getY());
			}
		} else {
			float anguloEmRadianos = interpoladorDoAngulo.interpole(intervalo);

			if (interpoladorDaEscalaX == null && interpoladorDaEscalaY == null) {
				desenhe(alpha,
					anguloEmRadianos,
					ponto.getX(),
					ponto.getY());
			} else if (interpoladorDaEscalaY == null) {
				desenhe(alpha,
					interpoladorDaEscalaX.interpole(intervalo),
					1.0f,
					anguloEmRadianos,
					ponto.getX(),
					ponto.getY());
			} else if (interpoladorDaEscalaX == null) {
				desenhe(alpha,
					1.0f,
					interpoladorDaEscalaY.interpole(intervalo),
					anguloEmRadianos,
					ponto.getX(),
					ponto.getY());
			} else {
				desenhe(alpha,
					interpoladorDaEscalaX.interpole(intervalo),
					interpoladorDaEscalaY.interpole(intervalo),
					anguloEmRadianos,
					ponto.getX(),
					ponto.getY());
			}
		}
	}
}
