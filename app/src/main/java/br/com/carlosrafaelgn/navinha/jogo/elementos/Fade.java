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
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.Interpolador;

public final class Fade extends FadeEstatico {
	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	public interface Observador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void animacaoDoFadeTerminada(Fade fade);
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private float opacidadeMaxima, tempoDaAnimacao, duracaoDaAnimacao;
	private boolean observadorAvisado;
	private Observador observador;
	private Interpolador interpolador;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Fade(FolhaDeSprites folhaDeSprites, float duracaoDaAnimacao, boolean entrada) {
		super(folhaDeSprites, entrada ? 1.0f : 0.0f);

		setOpacidadeMaxima(1.0f);
		setDuracaoDaAnimacao(duracaoDaAnimacao);

		// Se for um fade de entrada, inverte a opacidade (ela passa a variar de 1 a 0)
		setInterpolador(entrada ?
			Interpolador.crieInversor(Interpolador.crieAceleradoDesacelerado()) :
			Interpolador.crieAceleradoDesacelerado()
		);

		carregueInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public float getOpacidadeMaxima() {
		return opacidadeMaxima;
	}

	public void setOpacidadeMaxima(float opacidadeMaxima) {
		if (opacidadeMaxima < 0.0f || opacidadeMaxima > 1.0f) {
			throw new IllegalArgumentException("opacidadeMaxima deve ser >= 0 e <= 1");
		}

		this.opacidadeMaxima = opacidadeMaxima;
	}

	public float getTempoDaAnimacao() {
		return tempoDaAnimacao;
	}

	private void setTempoDaAnimacao(float tempoDaAnimacao) {
		this.tempoDaAnimacao = tempoDaAnimacao;
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

	public boolean isAnimacaoTerminada() {
		return (getTempoDaAnimacao() >= getDuracaoDaAnimacao());
	}

	public Observador getObservador() {
		return observador;
	}

	public void setObservador(Observador observador) {
		this.observador = observador;
	}

	private Interpolador getInterpolador() {
		return interpolador;
	}

	private void setInterpolador(Interpolador interpolador) {
		this.interpolador = interpolador;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setObservador(null);
		setInterpolador(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe FadeEstatico, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void processeUmQuadro(float deltaSegundos) {
		float tempoDaAnimacao = getTempoDaAnimacao() + deltaSegundos;
		float duracaoDaAnimacao = getDuracaoDaAnimacao();

		// Não podemos deixar que a animação passe do final
		if (tempoDaAnimacao >= duracaoDaAnimacao) {
			// Não podemos deixar que a animação passe do final
			setTempoDaAnimacao(duracaoDaAnimacao);

			// Avisa, apenas uma vez, quem estava nos observando
			if (!isObservadorAvisado()) {
				setObservadorAvisado(true);

				Observador observador = getObservador();
				if (observador != null) {
					observador.animacaoDoFadeTerminada(this);
				}
			}
		} else {
			setTempoDaAnimacao(tempoDaAnimacao);
		}

		// Calcula a opacidade de modo que ela varie de 0 a 1 ao longo do tempo da animação
		setOpacidade(getOpacidadeMaxima() * getInterpolador().interpole(getTempoDaAnimacao() / getDuracaoDaAnimacao()));
	}
}
