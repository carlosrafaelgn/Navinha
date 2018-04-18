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

import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.VetorFloat;

final class InterpoladorDeValores extends Interpolador {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	// Os valores e os intervalos, como vêm de fora da classe, são armazenados em um VetorFloat para
	// garantir que sejam imutáveis
	private final Interpolador interpoladorInterno;
	private final VetorFloat valores, intervalos;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	InterpoladorDeValores(Interpolador interpoladorInterno, VetorFloat valores, VetorFloat intervalos) {
		valideValoresEIntervalos(valores, intervalos);

		this.interpoladorInterno = interpoladorInterno;
		this.valores = valores;
		this.intervalos = intervalos;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private Interpolador getInterpoladorInterno() {
		return interpoladorInterno;
	}

	private VetorFloat getValores() {
		return valores;
	}

	private VetorFloat getIntervalos() {
		return intervalos;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public float interpole(float intervalo) {
		VetorFloat valores = getValores();

		// Os dois casos especiais (primeiro e último valores)
		if (intervalo <= 0.0f) {
			return valores.primeiro();
		} else if (intervalo >= 1.0f) {
			return valores.ultimo();
		}

		VetorFloat intervalos = getIntervalos();

		int indice = localizeIndiceDoIntervalo(intervalo, intervalos);

		// Faremos com que intervalo passe a valer algo entre 0 e 1, onde intervalos[indice] se
		// se transformará em 0, e intervalos[indice + 1] se transformará em 1
		intervalo -= intervalos.item(indice);
		intervalo /= (intervalos.item(indice + 1) - intervalos.item(indice));

		// Aplica a interpolação desejada (o método interpole() transformará a entrada, que
		// vale entre 0 e 1, em uma saída, também entre 0 e 1)
		intervalo = getInterpoladorInterno().interpole(intervalo);

		// Por fim, vamos transformar esse valor que varia de 0 a 1, no valor efetivamente desejado
		return (intervalo * (valores.item(indice + 1) - valores.item(indice))) + valores.item(indice);
	}
}
