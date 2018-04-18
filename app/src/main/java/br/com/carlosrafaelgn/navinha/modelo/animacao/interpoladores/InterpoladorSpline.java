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

//--------------------------------------------------------------------------------------------------
// A classe MatrizTriDiagonal e os métodos calculeCoeficientes e calculeSpline são minhas adaptações
// do código de Ryan Seghers, que pode ser encontrado na íntegra aqui:
// http://www.codeproject.com/Articles/560163/Csharp-Cubic-Spline-Interpolation
//
// Mais informações sobre Splines podem ser encontradas aqui:
// https://en.wikipedia.org/wiki/Spline_(mathematics)
// https://en.wikipedia.org/wiki/Spline_interpolation
// http://mathworld.wolfram.com/CubicSpline.html
//--------------------------------------------------------------------------------------------------

import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.VetorFloat;

final class InterpoladorSpline extends Interpolador {
	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	private static final class MatrizTriDiagonal {
		//
		// Author: Ryan Seghers
		//
		// Copyright (C) 2013-2014 Ryan Seghers
		//
		// Permission is hereby granted, free of charge, to any person obtaining
		// a copy of this software and associated documentation files (the
		// "Software"), to deal in the Software without restriction, including
		// without limitation the irrevocable, perpetual, worldwide, and royalty-free
		// rights to use, copy, modify, merge, publish, distribute, sublicense,
		// display, perform, create derivative works from and/or sell copies of
		// the Software, both in source and object code form, and to
		// permit persons to whom the Software is furnished to do so, subject to
		// the following conditions:
		//
		// The above copyright notice and this permission notice shall be
		// included in all copies or substantial portions of the Software.
		//
		// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
		// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
		// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
		// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
		// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
		// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
		// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
		//

		//----------------------------------------------------------------------------------------------
		// Campos privados
		//----------------------------------------------------------------------------------------------

		private final float[] a, b, c;

		//----------------------------------------------------------------------------------------------
		// Construtores
		//----------------------------------------------------------------------------------------------

		public MatrizTriDiagonal(int n) {
			a = new float[n];
			b = new float[n];
			c = new float[n];
		}

		//----------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//----------------------------------------------------------------------------------------------

		public float[] getA() {
			return a;
		}

		public float[] getB() {
			return b;
		}

		public float[] getC() {
			return c;
		}

		//----------------------------------------------------------------------------------------------
		// Métodos públicos
		//----------------------------------------------------------------------------------------------

		public float[] resolva(float[] d) {
			float[] a = getA();
			float[] b = getB();
			float[] c = getC();

			int n = a.length;

			// cPrime
			float[] cPrime = new float[n];
			cPrime[0] = c[0] / b[0];

			for (int i = 1; i < n; i++) {
				cPrime[i] = c[i] / (b[i] - cPrime[i-1] * a[i]);
			}

			// dPrime
			float[] dPrime = new float[n];
			dPrime[0] = d[0] / b[0];

			for (int i = 1; i < n; i++) {
				dPrime[i] = (d[i] - dPrime[i-1]* a[i]) / (b[i] - cPrime[i - 1] * a[i]);
			}

			float[] x = new float[n];
			x[n - 1] = dPrime[n - 1];

			for (int i = n-2; i >= 0; i--) {
				x[i] = dPrime[i] - cPrime[i] * x[i + 1];
			}

			return x;
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	// Os valores e os intervalos, como vêm de fora da classe, são armazenados em um VetorFloat para
	// garantir que sejam imutáveis, ao contrário dos coeficientes, que são gerados e utilizados
	// apenas pela própria classe (onde já conseguimos garantir a imutabilidade sem precisar
	// utilizar a classe VetorFloat)
	private final VetorFloat valores, intervalos;
	private final float[] coeficientesA, coeficientesB;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public InterpoladorSpline(VetorFloat valores, VetorFloat intervalos) {
		valideValoresEIntervalos(valores, intervalos);

		// Calcula os coeficientes da curva
		int n = intervalos.comprimento();

		float[] coeficientesA = new float[n - 1];
		float[] coeficientesB = new float[n - 1];

		float[] r = new float[n];

		MatrizTriDiagonal m = new MatrizTriDiagonal(n);

		float[] a = m.getA();
		float[] b = m.getB();
		float[] c = m.getC();

		float dx1, dx2, dy1, dy2;

		dx1 = intervalos.item(1) - intervalos.primeiro();
		c[0] = 1.0f / dx1;
		b[0] = 2.0f * c[0];
		r[0] = 3 * (valores.item(1) - valores.primeiro()) / (dx1 * dx1);

		for (int i = 1; i < n - 1; i++) {
			dx1 = intervalos.item(i) - intervalos.item(i - 1);
			dx2 = intervalos.item(i + 1) - intervalos.item(i);

			a[i] = 1.0f / dx1;
			c[i] = 1.0f / dx2;
			b[i] = 2.0f * (a[i] + c[i]);

			dy1 = valores.item(i) - valores.item(i - 1);
			dy2 = valores.item(i + 1) - valores.item(i);
			r[i] = 3 * (dy1 / (dx1 * dx1) + dy2 / (dx2 * dx2));
		}

		dx1 = intervalos.item(n - 1) - intervalos.item(n - 2);
		dy1 = valores.item(n - 1) - valores.item(n - 2);
		a[n - 1] = 1.0f / dx1;
		b[n - 1] = 2.0f * a[n - 1];
		r[n - 1] = 3 * (dy1 / (dx1 * dx1));

		float[] k = m.resolva(r);

		for (int i = 1; i < n; i++) {
			dx1 = intervalos.item(i) - intervalos.item(i - 1);
			dy1 = valores.item(i) - valores.item(i - 1);
			coeficientesA[i - 1] = k[i - 1] * dx1 - dy1;
			coeficientesB[i - 1] = -k[i] * dx1 + dy1;
		}

		this.valores = valores;
		this.intervalos = intervalos;
		this.coeficientesA = coeficientesA;
		this.coeficientesB = coeficientesB;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private VetorFloat getValores() {
		return valores;
	}

	private VetorFloat getIntervalos() {
		return intervalos;
	}

	private float[] getCoeficientesA() {
		return coeficientesA;
	}

	private float[] getCoeficientesB() {
		return coeficientesB;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	float interpole(float intervalo, int indiceDoIntervalo) {
		VetorFloat valores = getValores();
		VetorFloat intervalos = getIntervalos();
		float[] coeficientesA = getCoeficientesA();
		float[] coeficientesB = getCoeficientesB();
		float t = (intervalo - intervalos.item(indiceDoIntervalo)) / (intervalos.item(indiceDoIntervalo + 1) - intervalos.item(indiceDoIntervalo));
		return ((1 - t) * valores.item(indiceDoIntervalo)) + (t * valores.item(indiceDoIntervalo + 1)) + (t * (1 - t) * (coeficientesA[indiceDoIntervalo] * (1 - t) + coeficientesB[indiceDoIntervalo] * t));
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

		return interpole(intervalo, localizeIndiceDoIntervalo(intervalo, getIntervalos()));
	}
}
