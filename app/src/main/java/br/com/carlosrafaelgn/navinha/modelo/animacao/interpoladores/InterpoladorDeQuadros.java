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
package br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores;

import br.com.carlosrafaelgn.navinha.modelo.animacao.contadores.Contador;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.Vetor;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeTextura;

public final class InterpoladorDeQuadros {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private final Contador contador;
	private Vetor<CoordenadasDeTextura> quadros;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public InterpoladorDeQuadros(CoordenadasDeTextura quadro) {
		this(new Vetor<>(quadro), Contador.CONSTANTE, 0.0f);
	}

	public InterpoladorDeQuadros(Vetor<CoordenadasDeTextura> quadros, int tipoDoContador, float quadrosPorSegundo) {
		// Vamos criar um contador de 0 a 1, e deixar que setQuadros() ajuste
		this.contador = Contador.criePorTipo(tipoDoContador, 0.0f, 1.0f, 0.0f, quadrosPorSegundo);
		setQuadros(quadros);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private Contador getContador() {
		return contador;
	}

	public Vetor<CoordenadasDeTextura> getQuadros() {
		return quadros;
	}

	public void setQuadros(Vetor<CoordenadasDeTextura> quadros) {
		// Ao trocar o conjunto de quadros, vamos tentar manter o quadro atual
		Vetor<CoordenadasDeTextura> quadrosAntigos = getQuadros();

		this.quadros = quadros;

		if (quadrosAntigos != null && quadrosAntigos.comprimento() == quadros.comprimento()) {
			// Nada a fazer para ajustar o quadro atual
			return;
		}

		// Redefine o valor do quadro atual, apenas para ter certeza de que tudo vai dar certo
		setIndiceDoQuadroAtual(getIndiceDoQuadroAtual());
	}

	public final CoordenadasDeTextura getQuadro(int indice) {
		return getQuadros().item(indice);
	}

	public final CoordenadasDeTextura getQuadroAtual() {
		return getQuadro(getIndiceDoQuadroAtual());
	}

	public float getQuadrosPorSegundo() {
		return getContador().getIncrementoPorSegundo();
	}

	public void setQuadrosPorSegundo(float quadrosPorSegundo) {
		getContador().setIncrementoPorSegundo(quadrosPorSegundo);
	}

	public int getContagemDeQuadros() {
		return getQuadros().comprimento();
	}

	public int getIndiceDoQuadroAtual() {
		return (int)getContador().getValor();
	}

	public void setIndiceDoQuadroAtual(int indiceDoQuadroAtual) {
		if (indiceDoQuadroAtual >= quadros.comprimento()) {
			// Se não for possível utilizar o quadro pedido, volta para o quadro 0
			indiceDoQuadroAtual = 0;
		}

		Contador contador = getContador();

		// Se o valor máximo do contador é exclusivo, então é possível utilizar a contagem total de
		// quadros, caso contrário, precisamos subtrair 1
		if (contador.isMaximoExclusivo()) {
			getContador().reinicie(0.0f, (float)getContagemDeQuadros(), (float)indiceDoQuadroAtual);
		} else {
			getContador().reinicie(0.0f, (float)(getContagemDeQuadros() - 1), (float)indiceDoQuadroAtual);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public CoordenadasDeTextura interpoleDelta(float deltaSegundos) {
		return getQuadro((int)getContador().conte(deltaSegundos));
	}
}
