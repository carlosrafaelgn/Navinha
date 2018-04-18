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

final class InterpoladorDePontosLinear extends InterpoladorDePontos {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private final float[] coeficientesX, coeficientesY;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	InterpoladorDePontosLinear(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		super(coordenadasX, coordenadasY, intervalos);

		// Os coeficientes existem para acelerar um processo que será repetido muitas vezes
		// No nosso caso, teremos que calcular a equação da reta diversas vezes para os mesmos dois
		// pontos, fazendo sentido pré-calcular
		// O contra-ponto dessa técnica é que ela utiliza mais memória (vamos ver outra técnica na
		// classe InterpoladorDePontosAceleradoDesacelerado)

		float[] coeficientesX = new float[intervalos.comprimento() - 1];
		float[] coeficientesY = new float[intervalos.comprimento() - 1];

		for (int i = intervalos.comprimento() - 2; i >= 0; i--) {
			// Esses coeficientes são utilizados para transformar um valor de intervalo em outro
			// valor entre 0 e 1
			float delta = intervalos.item(i + 1) - intervalos.item(i);
			coeficientesX[i] = (coordenadasX.item(i + 1) - coordenadasX.item(i)) / delta;
			coeficientesY[i] = (coordenadasY.item(i + 1) - coordenadasY.item(i)) / delta;
		}

		this.coeficientesX = coeficientesX;
		this.coeficientesY = coeficientesY;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private float[] getCoeficientesX() {
		return coeficientesX;
	}

	private float[] getCoeficientesY() {
		return coeficientesY;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

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

		// Faremos com que intervalo passe a valer algo entre 0 e 1, onde intervalos[indice] se
		// se transformará em 0, e intervalos[indice + 1] se transformará em 1 (compare com o que é
		// feito em InterpoladorDePontosAceleradoDesacelerado, e veja como o uso de coeficientes
		// diminui a quantidade de cálculos)
		intervalo -= getIntervalos().item(indice);

		ponto.setX((intervalo * getCoeficientesX()[indice]) + getCoordenadasX().item(indice));
		ponto.setY((intervalo * getCoeficientesY()[indice]) + getCoordenadasY().item(indice));

		return ponto;
	}
}
