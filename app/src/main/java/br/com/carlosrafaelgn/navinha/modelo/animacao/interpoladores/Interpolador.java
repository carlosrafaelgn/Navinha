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

public abstract class Interpolador {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private static Interpolador constante, linear, acelerado, desacelerado, aceleradoDesacelerado, degrau;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	Interpolador() {
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private static VetorFloat crieIntervalosIgualmenteEspacados(int quantidade) {
		// Os casos básicos são tratados aqui mesmo
		switch (quantidade) {
		case 2:
			return new VetorFloat(0.0f, 1.0f);
		case 3:
			return new VetorFloat(0.0f, 0.5f, 1.0f);
		case 4:
			return new VetorFloat(0.0f, 0.33333333f, 0.66666666f, 1.0f);
		case 5:
			return new VetorFloat(0.0f, 0.2f, 0.4f, 0.6f, 1.0f);
		default:
			if (quantidade < 2) {
				throw new IllegalArgumentException("quantidade deve ser >= 2");
			}
			break;
		}

		// Todos os outros casos precisam ser calculados

		float[] intervalos = new float[quantidade];

		// O primeiro e o último intervalos são fáceis
		intervalos[0] = 0.0f;
		intervalos[quantidade - 1] = 1.0f;

		// Preenche do penúltimo até o segundo
		for (int i = quantidade - 2; i >= 1; i--) {
			intervalos[i] = (float)i / (float)(quantidade - 1);
		}

		return new VetorFloat(intervalos);
	}

	static void valideValoresEIntervalos(VetorFloat valores, VetorFloat intervalos) {
		// Todos devem ter o mesmo comprimento
		if (intervalos.comprimento() != valores.comprimento()) {
			throw new IllegalArgumentException("Comprimentos diferentes");
		}
		// Vamos verificar se todos os intervalos são maiores que seus predecessores, além de
		// começarem em 0, e terminarem em 1
		if (intervalos.primeiro() != 0.0f || intervalos.ultimo() != 1.0f) {
			throw new IllegalArgumentException("intervalos contém valores inválidos");
		}
		for (int i = intervalos.comprimento() - 1; i > 0; i--) {
			if (intervalos.item(i) <= intervalos.item(i - 1)) {
				throw new IllegalArgumentException("intervalos contém valores inválidos");
			}
		}
	}

	static int localizeIndiceDoIntervalo(float intervalo, VetorFloat intervalos) {
		// Os dois casos especiais (primeiro e último pontos)
		if (intervalo <= 0.0f) {
			return 0;
		} else if (intervalo >= 1.0f) {
			return intervalos.comprimento() - 2;
		}

		// Para os pontos intermediários, visto que intervalos está em ordem crescente, é possível
		// utilizar uma versão da busca binária para localizar o índice do intervalo em questão
		int inicio = 0, fim = intervalos.comprimento() - 1, meio;
		do {
			meio = (fim + inicio) >> 1;
			if (intervalo < intervalos.item(meio)) {
				fim = meio - 1;
			} else if (intervalo >= intervalos.item(meio + 1)) {
				inicio = meio + 1;
			} else {
				// Encontramos!
				break;
			}
		} while (fim >= inicio);

		return meio;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public static Interpolador crieConstante() {
		if (constante == null) {
			constante = new InterpoladorConstante();
		}
		return constante;
	}

	public static Interpolador crieLinear() {
		if (linear == null) {
			linear = new InterpoladorLinear();
		}
		return linear;
	}

	public static Interpolador crieAcelerado() {
		if (acelerado == null) {
			acelerado = new InterpoladorAcelerado();
		}
		return acelerado;
	}

	public static Interpolador crieDesacelerado() {
		if (desacelerado == null) {
			desacelerado = new InterpoladorDesacelerado();
		}
		return desacelerado;
	}

	public static Interpolador crieAceleradoDesacelerado() {
		if (aceleradoDesacelerado == null) {
			aceleradoDesacelerado = new InterpoladorAceleradoDesacelerado();
		}
		return aceleradoDesacelerado;
	}

	public static Interpolador crieDegrau() {
		if (degrau == null) {
			degrau = new InterpoladorDegrau();
		}
		return degrau;
	}

	public static Interpolador crieValores(Interpolador interpoladorInterno, VetorFloat valores) {
		return new InterpoladorDeValores(interpoladorInterno, valores, crieIntervalosIgualmenteEspacados(valores.comprimento()));
	}

	public static Interpolador crieValores(Interpolador interpoladorInterno, VetorFloat valores, VetorFloat intervalos) {
		return new InterpoladorDeValores(interpoladorInterno, valores, intervalos);
	}

	public static Interpolador crieSpline(VetorFloat valores) {
		return new InterpoladorSpline(valores, crieIntervalosIgualmenteEspacados(valores.comprimento()));
	}

	public static Interpolador crieSpline(VetorFloat valores, VetorFloat intervalos) {
		return new InterpoladorSpline(valores, intervalos);
	}

	public static Interpolador crieInversor(Interpolador original) {
		return new InterpoladorInversor(original);
	}

	// Para que nossos interpoladores funcionem, intervalo deve estar entre 0 e 1 (inclusive)
	// Alguns interpoladores podem funcionar com valores fora desse inervalo, enquanto que outros
	// podem simplemente retornar valores inválidos caso intervalo não esteja entre 0 e 1!
	public abstract float interpole(float intervalo);
}
