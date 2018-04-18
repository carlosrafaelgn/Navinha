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
package br.com.carlosrafaelgn.navinha.modelo.animacao.contadores;

final class ContadorPingPong extends Contador {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private float maximoPingPong, valorPingPong;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public ContadorPingPong(float minimo, float maximo, float valor, float incrementoPorSegundo) {
		super(minimo, maximo, valor, incrementoPorSegundo);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private float getMaximoPingPong() {
		return maximoPingPong;
	}

	private void setMaximoPingPong(float maximoPingPong) {
		this.maximoPingPong = maximoPingPong;
	}

	private float getValorPingPong() {
		return valorPingPong;
	}

	private void setValorPingPong(float valorPingPong) {
		this.valorPingPong = valorPingPong;
	}

	@Override
	public boolean isMaximoExclusivo() {
		return false;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void reinicie(float minimo, float maximo, float valor) {
		super.reinicie(minimo, maximo, valor);

		float delta = maximo - minimo;
		setMaximoPingPong(2.0f * delta);
		setValorPingPong(delta + (valor - minimo));
	}

	@Override
	public float conte(float deltaSegundos) {
		float valorPingPong = getValorPingPong() + (getIncrementoPorSegundo() * deltaSegundos);

		float maximoPingPong = getMaximoPingPong();

		// Repare que o operador de resto de divisão % poderia ser utilizado aqui, mas para esse
		// caso específico, o desempenho do while é melhor
		while (valorPingPong >= maximoPingPong) {
			valorPingPong -= maximoPingPong;
		}

		setValorPingPong(valorPingPong);

		// Gera uma onda triangular, que oscila entre o valor mínimo e o máximo
		float valor = getMinimo() + Math.abs(valorPingPong - (0.5f * maximoPingPong));

		setValor(valor);

		return valor;
	}
}
