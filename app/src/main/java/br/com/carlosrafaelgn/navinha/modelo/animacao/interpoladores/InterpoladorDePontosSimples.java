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
import br.com.carlosrafaelgn.navinha.modelo.desenho.Ponto;

final class InterpoladorDePontosSimples extends InterpoladorDePontos {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private final Interpolador interpoladorInterno;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	InterpoladorDePontosSimples(Interpolador interpoladorInterno, VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		super(coordenadasX, coordenadasY, intervalos);

		this.interpoladorInterno = interpoladorInterno;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private Interpolador getInterpoladorInterno() {
		return interpoladorInterno;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public Ponto interpole(float intervalo) {
		Ponto ponto = getUltimoPontoInterpolado();

		// Primeiro vamos determinar se é realmente necessário algum tipo de cálculo
		int indice = localizeIndiceDoIntervalo(intervalo);
		if (indice < 0) {
			return ponto;
		}

		VetorFloat coordenadasX = getCoordenadasX();
		VetorFloat coordenadasY = getCoordenadasY();
		VetorFloat intervalos = getIntervalos();

		// Faremos com que intervalo passe a valer algo entre 0 e 1, onde intervalos[indice] se
		// se transformará em 0, e intervalos[indice + 1] se transformará em 1
		intervalo -= intervalos.item(indice);
		intervalo /= (intervalos.item(indice + 1) - intervalos.item(indice));

		// Aplica a interpolação desejada (o método interpole() transformará a entrada, que
		// vale entre 0 e 1, em uma saída, também entre 0 e 1)
		intervalo = getInterpoladorInterno().interpole(intervalo);

		// Por fim, vamos transformar esse valor que varia de 0 a 1, no valor efetivamente desejado
		ponto.setX((intervalo * (coordenadasX.item(indice + 1) - coordenadasX.item(indice))) + coordenadasX.item(indice));
		ponto.setY((intervalo * (coordenadasY.item(indice + 1) - coordenadasY.item(indice))) + coordenadasY.item(indice));

		return ponto;
	}
}
