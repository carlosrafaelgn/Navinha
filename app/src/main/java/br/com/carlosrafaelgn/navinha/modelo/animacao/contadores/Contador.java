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

public abstract class Contador {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	public static final int CONSTANTE = 0;
	public static final int LOOPING = 1;
	public static final int PING_PONG = 2;
	public static final int PING_PONG_INTEIRO = 3;
	public static final int UMA_VEZ = 4;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private float minimo, maximo, valor, incrementoPorSegundo;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	Contador(float minimo, float maximo, float valor, float incrementoPorSegundo) {
		reinicie(minimo, maximo, valor);
		setIncrementoPorSegundo(incrementoPorSegundo);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public final float getMinimo() {
		return minimo;
	}

	private void setMinimo(float minimo) {
		this.minimo = minimo;
	}

	public final float getMaximo() {
		return maximo;
	}

	private void setMaximo(float maximo) {
		this.maximo = maximo;
	}

	public final float getValor() {
		return valor;
	}

	protected final void setValor(float valor) {
		this.valor = valor;
	}

	public final float getIncrementoPorSegundo() {
		return incrementoPorSegundo;
	}

	public final void setIncrementoPorSegundo(float incrementoPorSegundo) {
		this.incrementoPorSegundo = incrementoPorSegundo;
	}

	public abstract boolean isMaximoExclusivo();

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	// Tipos de contador:
	// Constante: sempre retorna o mesmo valor
	// Looping: um "dente de serra" partindo de mínimo, até (mas não incluindo) máximo
	// PingPong: uma onda triangular, indo de mínimo até máximo, depois voltando para mínimo e assim
	// por diante
	// PingPongInteiro: idem ao PingPong, porém os valores são sempre inteiros, criando "degraus" na
	// saída
	// UmaVez: começa em mínimo, e ao chegar a máximo, permanece em máximo
	public static Contador crieConstante(float valor) {
		return new ContadorConstante(valor);
	}

	public static Contador crieLooping(float minimo, float maximo, float valor, float incrementoPorSegundo) {
		return new ContadorLooping(minimo, maximo, valor, incrementoPorSegundo);
	}

	public static Contador criePingPong(float minimo, float maximo, float valor, float incrementoPorSegundo) {
		return new ContadorPingPong(minimo, maximo, valor, incrementoPorSegundo);
	}

	public static Contador criePingPongInteiro(float minimo, float maximo, float valor, float incrementoPorSegundo) {
		return new ContadorPingPongInteiro(minimo, maximo, valor, incrementoPorSegundo);
	}

	public static Contador crieUmaVez(float minimo, float maximo, float valor, float incrementoPorSegundo) {
		return new ContadorUmaVez(minimo, maximo, valor, incrementoPorSegundo);
	}

	public static Contador criePorTipo(int tipo, float minimo, float maximo, float valor, float incrementoPorSegundo) {
		switch (tipo) {
		case CONSTANTE:
			return new ContadorConstante(valor);
		case LOOPING:
			return new ContadorLooping(minimo, maximo, valor, incrementoPorSegundo);
		case PING_PONG:
			return new ContadorPingPong(minimo, maximo, valor, incrementoPorSegundo);
		case PING_PONG_INTEIRO:
			return new ContadorPingPongInteiro(minimo, maximo, valor, incrementoPorSegundo);
		case UMA_VEZ:
			return new ContadorUmaVez(minimo, maximo, valor, incrementoPorSegundo);
		}

		throw new IllegalArgumentException("tipo inválido");
	}

	public void reinicie(float minimo, float maximo, float valor) {
		if (minimo > maximo) {
			throw new IllegalArgumentException("minimo deve ser <= maximo");
		}
		if (valor < minimo) {
			throw new IllegalArgumentException("valor deve ser >= minimo");
		}
		if (valor > maximo) {
			throw new IllegalArgumentException("valor deve ser <= maximo");
		}
		setMinimo(minimo);
		setMaximo(maximo);
		setValor(valor);
	}

	public abstract float conte(float deltaSegundos);
}
