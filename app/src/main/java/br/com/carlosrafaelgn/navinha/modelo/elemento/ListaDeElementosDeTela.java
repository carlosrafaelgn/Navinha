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
package br.com.carlosrafaelgn.navinha.modelo.elemento;

import java.util.ArrayList;

import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public final class ListaDeElementosDeTela extends Recurso {
	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	private static class AcaoPendente {
		//------------------------------------------------------------------------------------------
		// Constantes
		//------------------------------------------------------------------------------------------

		public static final int ADICIONE_AO_INICIO = 0;
		public static final int ADICIONE_AO_FINAL = 1;
		public static final int ADICIONE_ACIMA = 2;
		public static final int REMOVA = 3;
		public static final int REMOVA_E_DESTRUA = 4;
		public static final int MOVA_PARA_O_INICIO = 5;
		public static final int MOVA_PARA_O_FINAL = 6;
		public static final int SUBA_UM_NIVEL = 7;
		public static final int DESCA_UM_NIVEL = 8;

		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private AcaoPendente acaoSeguinte;
		private ElementoDeTela elementoDeTela, elementoDeReferencia;
		private int tipoAcao;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public AcaoPendente(ElementoDeTela elementoDeTela, ElementoDeTela elementoDeReferencia, int tipoAcao) {
			setElementoDeTela(elementoDeTela);
			setElementoDeReferencia(elementoDeReferencia);
			setTipoAcao(tipoAcao);
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		public AcaoPendente getAcaoSeguinte() {
			return acaoSeguinte;
		}

		public void setAcaoSeguinte(AcaoPendente acaoSeguinte) {
			this.acaoSeguinte = acaoSeguinte;
		}

		private ElementoDeTela getElementoDeTela() {
			return elementoDeTela;
		}

		private void setElementoDeTela(ElementoDeTela elementoDeTela) {
			this.elementoDeTela = elementoDeTela;
		}

		private ElementoDeTela getElementoDeReferencia() {
			return elementoDeReferencia;
		}

		private void setElementoDeReferencia(ElementoDeTela elementoDeReferencia) {
			this.elementoDeReferencia = elementoDeReferencia;
		}

		private int getTipoAcao() {
			return tipoAcao;
		}

		private void setTipoAcao(int tipoAcao) {
			this.tipoAcao = tipoAcao;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		public void carregue() {
			carregueComSeguranca(getElementoDeTela());
			carregueComSeguranca(getElementoDeReferencia());
		}

		public void libere() {
			libereComSeguranca(getElementoDeTela());
			libereComSeguranca(getElementoDeReferencia());
		}

		public void destrua() {
			// Vamos invalidar o objeto

			destruaComSeguranca(getElementoDeTela());
			destruaComSeguranca(getElementoDeReferencia());

			setAcaoSeguinte(null);
			setElementoDeTela(null);
			setElementoDeReferencia(null);
		}

		public void execute(ListaDeElementosDeTela listaDeElementosDeTela) {
			switch (getTipoAcao()) {
			case ADICIONE_AO_INICIO:
				listaDeElementosDeTela.adicioneAoInicio(getElementoDeTela());
				break;
			case ADICIONE_AO_FINAL:
				listaDeElementosDeTela.adicioneAoFinal(getElementoDeTela());
				break;
			case ADICIONE_ACIMA:
				listaDeElementosDeTela.adicioneAcima(getElementoDeTela(), getElementoDeReferencia());
				break;
			case REMOVA:
				listaDeElementosDeTela.remova(getElementoDeTela());
				break;
			case REMOVA_E_DESTRUA:
				listaDeElementosDeTela.removaEDestrua(getElementoDeTela());
				break;
			case MOVA_PARA_O_INICIO:
				listaDeElementosDeTela.movaParaOInicio(getElementoDeTela());
				break;
			case MOVA_PARA_O_FINAL:
				listaDeElementosDeTela.movaParaOFinal(getElementoDeTela());
				break;
			case SUBA_UM_NIVEL:
				listaDeElementosDeTela.subaUmNivel(getElementoDeTela());
				break;
			case DESCA_UM_NIVEL:
				listaDeElementosDeTela.descaUmNivel(getElementoDeTela());
				break;
			}

			setAcaoSeguinte(null);
			setElementoDeTela(null);
			setElementoDeReferencia(null);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	// No que diz respeito à ordem de desenho na tela, o primeiro elemento está acima de todos os
	// outros, enquanto que o último elemento está abaixo de todos os outros
	private ElementoDeTela primeiroElemento, ultimoElemento;
	private AcaoPendente primeiraAcaoPendente, ultimaAcaoPendente, primeiraRemocaoPendente, ultimaRemocaoPendente;
	private int contagemDeElementos;
	private boolean processando, carregado;

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private ElementoDeTela getPrimeiroElemento() {
		return primeiroElemento;
	}

	private void setPrimeiroElemento(ElementoDeTela primeiroElemento) {
		this.primeiroElemento = primeiroElemento;
	}

	private ElementoDeTela getUltimoElemento() {
		return ultimoElemento;
	}

	private void setUltimoElemento(ElementoDeTela ultimoElemento) {
		this.ultimoElemento = ultimoElemento;
	}

	private AcaoPendente getPrimeiraAcaoPendente() {
		return primeiraAcaoPendente;
	}

	private void setPrimeiraAcaoPendente(AcaoPendente primeiraAcaoPendente) {
		this.primeiraAcaoPendente = primeiraAcaoPendente;
	}

	private AcaoPendente getUltimaAcaoPendente() {
		return ultimaAcaoPendente;
	}

	private void setUltimaAcaoPendente(AcaoPendente ultimaAcaoPendente) {
		this.ultimaAcaoPendente = ultimaAcaoPendente;
	}

	private AcaoPendente getPrimeiraRemocaoPendente() {
		return primeiraRemocaoPendente;
	}

	private void setPrimeiraRemocaoPendente(AcaoPendente primeiraRemocaoPendente) {
		this.primeiraRemocaoPendente = primeiraRemocaoPendente;
	}

	private AcaoPendente getUltimaRemocaoPendente() {
		return ultimaRemocaoPendente;
	}

	private void setUltimaRemocaoPendente(AcaoPendente ultimaRemocaoPendente) {
		this.ultimaRemocaoPendente = ultimaRemocaoPendente;
	}

	public int getContagemDeElementos() {
		return contagemDeElementos;
	}

	private void setContagemDeElementos(int contagemDeElementos) {
		this.contagemDeElementos = contagemDeElementos;
	}

	private boolean isProcessando() {
		return processando;
	}

	private void setProcessando(boolean processando) {
		this.processando = processando;
	}

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return carregado;
	}

	private void setCarregado(boolean carregado) {
		this.carregado = carregado;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private void adicioneAcaoPendente(ElementoDeTela elementoDeTela, ElementoDeTela elementoDeReferencia, int tipoAcao) {
		// Para as ações pendentes, nós mantemos uma lista ligada simples, com uma ressalva: ações
		// de remoção devem ficar em uma lista separada, pois todas as remoções devem acontecer
		// depois das demais (um caso prático: uma adição usando o elemento X como referência está
		// agendada para o mesmo quadro que a remoção de X)

		AcaoPendente acaoPendente = new AcaoPendente(elementoDeTela, elementoDeReferencia, tipoAcao);

		switch (tipoAcao) {
		case AcaoPendente.REMOVA:
		case AcaoPendente.REMOVA_E_DESTRUA:
			// Esse elemento não deve mais ser processado/desenhado
			elementoDeTela.setMarcadoParaRemocao(true);

			if (getPrimeiraRemocaoPendente() == null) {
				setPrimeiraRemocaoPendente(acaoPendente);
			}

			AcaoPendente ultimaRemocaoPendente = getUltimaRemocaoPendente();

			if (ultimaRemocaoPendente != null) {
				ultimaRemocaoPendente.setAcaoSeguinte(acaoPendente);
			}

			setUltimaRemocaoPendente(acaoPendente);
			return;
		}

		if (getPrimeiraAcaoPendente() == null) {
			setPrimeiraAcaoPendente(acaoPendente);
		}

		AcaoPendente ultimaAcaoPendente = getUltimaAcaoPendente();

		if (ultimaAcaoPendente != null) {
			ultimaAcaoPendente.setAcaoSeguinte(acaoPendente);
		}

		setUltimaAcaoPendente(acaoPendente);
	}

	private ElementoDeTela primeiroElementoQueColideForcaBruta(ElementoDeTela elementoParaTestar) {
		// Basta percorrer todos os elementos até que se encontre um que faça intersecção com
		// o elemento desejado (começa pelo primeiro elemento, que é o elemento mais à frente na
		// tela)
		ElementoDeTela elementoDeTela = getPrimeiroElemento();

		while (elementoDeTela != null) {
			if (elementoDeTela.isParteDoSistemaDeColisoes() && elementoDeTela.colideCom(elementoParaTestar)) {
				return elementoDeTela;
			}
			elementoDeTela = elementoDeTela.getElementoAnterior();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private <E extends ElementoDeTela> E primeiroElementoQueColideForcaBruta(ElementoDeTela elementoParaTestar, Class<E> classeDesejada) {
		// Basta percorrer todos os elementos até que se encontre um que faça intersecção com
		// o elemento desejado (começa pelo primeiro elemento, que é o elemento mais à frente na
		// tela)
		ElementoDeTela elementoDeTela = getPrimeiroElemento();

		while (elementoDeTela != null) {
			if (elementoDeTela.isParteDoSistemaDeColisoes() && elementoDeTela.colideCom(elementoParaTestar) && classeDesejada.isAssignableFrom(elementoDeTela.getClass())) {
				return (E)elementoDeTela;
			}
			elementoDeTela = elementoDeTela.getElementoAnterior();
		}

		return null;
	}

	private ArrayList<ElementoDeTela> elementosQueColidemForcaBruta(ElementoDeTela elementoParaTestar) {
		// Basta percorrer todos os elementos, e ir adicionando à lista elementos todos os elementos
		// que fazem intersecção com o elemento desejado (começa pelo primeiro elemento, que é o
		// elemento mais à frente na tela)
		ArrayList<ElementoDeTela> elementos = new ArrayList<>();
		ElementoDeTela elementoDeTela = getPrimeiroElemento();

		while (elementoDeTela != null) {
			if (elementoDeTela.isParteDoSistemaDeColisoes() && elementoDeTela.colideCom(elementoParaTestar)) {
				elementos.add(elementoDeTela);
			}
			elementoDeTela = elementoDeTela.getElementoAnterior();
		}

		return elementos;
	}

	private ElementoDeTela primeiroElementoQueContemPontoForcaBruta(float x, float y) {
		// Basta percorrer todos os elementos até que se encontre um que contenha o ponto desejado
		// (começa pelo primeiro elemento, que é o elemento mais à frente na tela)
		ElementoDeTela elementoDeTela = getPrimeiroElemento();

		while (elementoDeTela != null) {
			if (elementoDeTela.isParteDoSistemaDeColisoes() && elementoDeTela.contemPonto(x, y)) {
				return elementoDeTela;
			}
			elementoDeTela = elementoDeTela.getElementoAnterior();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private <E extends ElementoDeTela> E primeiroElementoQueContemPontoForcaBruta(float x, float y, Class<E> classeDesejada) {
		// Basta percorrer todos os elementos até que se encontre um que contenha o ponto desejado
		// (começa pelo primeiro elemento, que é o elemento mais à frente na tela)
		ElementoDeTela elementoDeTela = getPrimeiroElemento();

		while (elementoDeTela != null) {
			if (elementoDeTela.isParteDoSistemaDeColisoes() && elementoDeTela.contemPonto(x, y) && classeDesejada.isAssignableFrom(elementoDeTela.getClass())) {
				return (E)elementoDeTela;
			}
			elementoDeTela = elementoDeTela.getElementoAnterior();
		}

		return null;
	}

	private ArrayList<ElementoDeTela> elementosQueContemPontoForcaBruta(float x, float y) {
		// Basta percorrer todos os elementos, e ir adicionando à lista elementos todos os elementos
		// que contenham o ponto desejado (começa pelo primeiro elemento, que é o elemento mais à
		// frente na tela)
		ArrayList<ElementoDeTela> elementos = new ArrayList<>();
		ElementoDeTela elementoDeTela = getPrimeiroElemento();

		while (elementoDeTela != null) {
			if (elementoDeTela.isParteDoSistemaDeColisoes() && elementoDeTela.contemPonto(x, y)) {
				elementos.add(elementoDeTela);
			}
			elementoDeTela = elementoDeTela.getElementoAnterior();
		}

		return elementos;
	}

	void areaLimiteAlterada(ElementoDeTela elementoDeTela) {
	}

	void adicioneAoSistemaDeColisoes(ElementoDeTela elementoDeTela) {
	}

	void removaDoSistemaDeColisoes(ElementoDeTela elementoDeTela) {
	}

	@Override
	public void carregueInternamente() {
		ElementoDeTela elementoDeTela = getUltimoElemento();
		while (elementoDeTela != null) {
			elementoDeTela.carregue();
			elementoDeTela = elementoDeTela.getElementoSeguinte();
		}

		AcaoPendente acaoPendente = getPrimeiraAcaoPendente();
		while (acaoPendente != null) {
			acaoPendente.carregue();
			acaoPendente = acaoPendente.getAcaoSeguinte();
		}

		AcaoPendente remocaoPendente = getPrimeiraRemocaoPendente();
		while (remocaoPendente != null) {
			remocaoPendente.carregue();
			remocaoPendente = remocaoPendente.getAcaoSeguinte();
		}

		// Utilizamos a propriedade carregado para indicar o carregamento, já que a lista de
		// elementos de tela não tem um objeto em especial que possa ser utilizado para isso
		setCarregado(true);
	}

	@Override
	public void libereInternamente() {
		ElementoDeTela elementoDeTela = getUltimoElemento();
		while (elementoDeTela != null) {
			elementoDeTela.libere();
			elementoDeTela = elementoDeTela.getElementoSeguinte();
		}

		AcaoPendente acaoPendente = getPrimeiraAcaoPendente();
		while (acaoPendente != null) {
			acaoPendente.libere();
			acaoPendente = acaoPendente.getAcaoSeguinte();
		}

		AcaoPendente remocaoPendente = getPrimeiraRemocaoPendente();
		while (remocaoPendente != null) {
			remocaoPendente.libere();
			remocaoPendente = remocaoPendente.getAcaoSeguinte();
		}

		// Utilizamos a propriedade carregado para indicar o carregamento, já que a lista de
		// elementos de tela não tem um objeto em especial que possa ser utilizado para isso
		setCarregado(false);
	}

	@Override
	public void destruaInternamente() {
		ElementoDeTela elementoDeTela = getUltimoElemento();
		while (elementoDeTela != null) {
			// Temos que manter uma referência para o próximo elemento aqui, pois o método destrua()
			// irá transformar tudo em null
			ElementoDeTela temporario = elementoDeTela.getElementoSeguinte();

			elementoDeTela.destrua();

			elementoDeTela = temporario;
		}

		AcaoPendente acaoPendente = getPrimeiraAcaoPendente();
		while (acaoPendente != null) {
			// Temos que manter uma referência para a próxima ação aqui, pois o método destrua()
			// irá transformar tudo em null
			AcaoPendente temporario = acaoPendente.getAcaoSeguinte();

			acaoPendente.destrua();

			acaoPendente = temporario;
		}

		AcaoPendente remocaoPendente = getPrimeiraRemocaoPendente();
		while (remocaoPendente != null) {
			// Temos que manter uma referência para a próxima ação aqui, pois o método destrua()
			// irá transformar tudo em null
			AcaoPendente temporario = remocaoPendente.getAcaoSeguinte();

			remocaoPendente.destrua();

			remocaoPendente = temporario;
		}

		// Vamos aproveitar que todos os elementos de tela foram destruídos, e invalidar o objeto
		setPrimeiroElemento(null);
		setUltimoElemento(null);
		setPrimeiraAcaoPendente(null);
		setUltimaAcaoPendente(null);
		setPrimeiraRemocaoPendente(null);
		setUltimaRemocaoPendente(null);
		setContagemDeElementos(0);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void adicioneAoInicio(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.ADICIONE_AO_INICIO);
			return;
		}

		// Primeiro vamos garantir que esse elemento não pertence a lista alguma
		if (elementoDeTela.getLista() != null) {
			throw new RuntimeException("Esse elemento já pertence a uma lista");
		}

		// O elemento pertence a essa lista agora
		elementoDeTela.setLista(this);

		// O elemento pode ser processado/desenhado
		elementoDeTela.setMarcadoParaRemocao(false);

		// Adiciona o elemento ao início lista
		ElementoDeTela primeiroElemento = getPrimeiroElemento();

		elementoDeTela.setElementoAnterior(primeiroElemento);
		elementoDeTela.setElementoSeguinte(null);

		if (primeiroElemento != null) {
			primeiroElemento.setElementoSeguinte(elementoDeTela);
		}

		setPrimeiroElemento(elementoDeTela);

		if (getUltimoElemento() == null) {
			// Se a lista estiver vazia, o primeiro também será o último
			setUltimoElemento(elementoDeTela);
		}

		setContagemDeElementos(getContagemDeElementos() + 1);
	}

	public void adicioneAoFinal(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.ADICIONE_AO_FINAL);
			return;
		}

		// Primeiro vamos garantir que esse elemento não pertence a lista alguma
		if (elementoDeTela.getLista() != null) {
			throw new RuntimeException("Esse elemento já pertence a uma lista");
		}

		// O elemento pertence a essa lista agora
		elementoDeTela.setLista(this);

		// O elemento pode ser processado/desenhado
		elementoDeTela.setMarcadoParaRemocao(false);

		// Adiciona o elemento ao final da lista
		ElementoDeTela ultimoElemento = getUltimoElemento();

		elementoDeTela.setElementoAnterior(null);
		elementoDeTela.setElementoSeguinte(ultimoElemento);

		if (ultimoElemento != null) {
			ultimoElemento.setElementoAnterior(elementoDeTela);
		}

		setUltimoElemento(elementoDeTela);

		if (getPrimeiroElemento() == null) {
			// Se a lista estiver vazia, o primeiro também será o último
			setPrimeiroElemento(elementoDeTela);
		}

		setContagemDeElementos(getContagemDeElementos() + 1);
	}

	public void adicioneAcima(ElementoDeTela elementoDeTela, ElementoDeTela elementoDeReferencia) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, elementoDeReferencia, AcaoPendente.ADICIONE_ACIMA);
			return;
		}

		ElementoDeTela primeiroElemento = getPrimeiroElemento();

		// Se o elemento de referência era o primeiro elemento, então basta adicionar elementoDeTela
		// ao início da lista
		if (elementoDeReferencia == primeiroElemento) {
			adicioneAoInicio(elementoDeTela);
			return;
		}

		// Primeiro vamos garantir que esse elemento não pertence a lista alguma
		if (elementoDeTela.getLista() != null) {
			throw new RuntimeException("Esse elemento já pertence a uma lista");
		}

		// E vamos garantir que o elemento de referência faz parte dessa lista
		if (elementoDeReferencia.getLista() != this) {
			throw new RuntimeException("O elemento de referência não pertence a essa lista");
		}

		// O elemento pertence a essa lista agora
		elementoDeTela.setLista(this);

		// O elemento pode ser processado/desenhado
		elementoDeTela.setMarcadoParaRemocao(false);

		ElementoDeTela seguinte = elementoDeReferencia.getElementoSeguinte();

		// Adiciona o elemento ao meio da lista
		elementoDeTela.setElementoAnterior(elementoDeReferencia);
		elementoDeTela.setElementoSeguinte(seguinte);

		// Atualiza o anterior do seguinte, e o seguinte de elementoDeReferencia
		seguinte.setElementoAnterior(elementoDeTela);
		elementoDeReferencia.setElementoSeguinte(elementoDeTela);

		setContagemDeElementos(getContagemDeElementos() + 1);
	}

	public void remova(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.REMOVA);
			return;
		}

		// Primeiro vamos garantir que esse elemento está na lista
		if (elementoDeTela.getLista() != this) {
			throw new RuntimeException("Esse elemento não pertence a essa lista");
		}

		elementoDeTela.setLista(null);

		ElementoDeTela anterior = elementoDeTela.getElementoAnterior();
		ElementoDeTela seguinte = elementoDeTela.getElementoSeguinte();

		// Vamos ajudar o garbage colletor :)
		elementoDeTela.setElementoAnterior(null);
		elementoDeTela.setElementoSeguinte(null);

		if (elementoDeTela == getPrimeiroElemento()) {
			// O elemento que vinha antes de elementoDeTela agora se tornou o primeiro
			setPrimeiroElemento(anterior);
		}

		if (elementoDeTela == getUltimoElemento()) {
			// O elemento que vinha depois de elementoDeTela agora se tornou o último
			setUltimoElemento(seguinte);
		}

		// Apenas atualiza o seguinte do anterior, e o anterior do seguine ;)
		if (anterior != null) {
			anterior.setElementoSeguinte(seguinte);
		}
		if (seguinte != null) {
			seguinte.setElementoAnterior(anterior);
		}

		setContagemDeElementos(getContagemDeElementos() - 1);
	}

	public void removaEDestrua(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.REMOVA_E_DESTRUA);
			return;
		}

		// Vamos reaproveitar os comportamentos já existentes desses outros dois métodos
		remova(elementoDeTela);
		elementoDeTela.destrua();
	}

	public void movaParaOInicio(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.MOVA_PARA_O_INICIO);
			return;
		}

		if (elementoDeTela == getPrimeiroElemento()) {
			// Não há mais nada que possa ser feito, o elemento já está na parte mais alta da lista
			return;
		}

		// Vamos reaproveitar os comportamentos já existentes desses outros dois métodos
		remova(elementoDeTela);
		adicioneAoInicio(elementoDeTela);
	}

	public void movaParaOFinal(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.MOVA_PARA_O_FINAL);
			return;
		}

		if (elementoDeTela == getUltimoElemento()) {
			// Não há mais nada que possa ser feito, o elemento já está na parte mais baixa da lista
			return;
		}

		// Vamos reaproveitar os comportamentos já existentes desses outros dois métodos
		remova(elementoDeTela);
		adicioneAoFinal(elementoDeTela);
	}

	public void subaUmNivel(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.SUBA_UM_NIVEL);
			return;
		}

		if (elementoDeTela == getPrimeiroElemento()) {
			// Não há mais nada que possa ser feito, o elemento já está na parte mais alta da lista
			return;
		}

		// Vamos reaproveitar os comportamentos já existentes desses outros métodos
		ElementoDeTela seguinte = elementoDeTela.getElementoSeguinte();
		remova(elementoDeTela);
		adicioneAcima(elementoDeTela, seguinte);
	}

	public void descaUmNivel(ElementoDeTela elementoDeTela) {
		// Se a lista estava no meio do processamento, não podemos deixar que seus elementos sejam
		// alterados, e por isso adicionamos uma ação pendente para ser executada no início do
		// próximo quadro
		if (isProcessando()) {
			adicioneAcaoPendente(elementoDeTela, null, AcaoPendente.DESCA_UM_NIVEL);
			return;
		}

		ElementoDeTela ultimoElemento = getUltimoElemento();

		if (elementoDeTela == ultimoElemento) {
			// Não há mais nada que possa ser feito, o elemento já está na parte mais baixa da lista
			return;
		}

		// Vamos reaproveitar os comportamentos já existentes desses outros métodos
		ElementoDeTela anterior = elementoDeTela.getElementoAnterior();

		if (anterior == ultimoElemento) {
			// Se o elemento que vinha antes de elementoDeTela era o último, então para fazer com
			// que ele desça um nível, basta adicioná-lo ao final da lista
			remova(elementoDeTela);
			adicioneAoFinal(elementoDeTela);
		} else {
			// Agora, se o elemento que vinha antes de elementoDeTela não era o último, então para
			// fazer com que ele desça um nível, basta adicioná-lo acima do elemento que vinha antes
			// de anterior
			remova(elementoDeTela);
			adicioneAcima(elementoDeTela, anterior.getElementoAnterior());
		}
	}

	public ElementoDeTela primeiroElementoQueColide(ElementoDeTela elementoParaTestar) {
		return primeiroElementoQueColideForcaBruta(elementoParaTestar);
	}

	public <E extends ElementoDeTela> E primeiroElementoQueColide(ElementoDeTela elementoParaTestar, Class<E> classeDesejada) {
		return primeiroElementoQueColideForcaBruta(elementoParaTestar, classeDesejada);
	}

	public ArrayList<ElementoDeTela> elementosQueColidem(ElementoDeTela elementoParaTestar) {
		return elementosQueColidemForcaBruta(elementoParaTestar);
	}

	public ElementoDeTela primeiroElementoQueContemPonto(float x, float y) {
		return primeiroElementoQueContemPontoForcaBruta(x, y);
	}

	public <E extends ElementoDeTela> E primeiroElementoQueContemPonto(float x, float y, Class<E> classeDesejada) {
		return primeiroElementoQueContemPontoForcaBruta(x, y, classeDesejada);
	}

	public ArrayList<ElementoDeTela> elementosQueContemPonto(float x, float y) {
		return elementosQueContemPontoForcaBruta(x, y);
	}

	public void processeEDesenheUmQuadro(float deltaSegundos) {
		// Essa indicação serve para controlar o comportamento dos métodos que alteram a lista, tais
		// como adicioneAoInicio, remova e assim por diante
		setProcessando(true);

		// Processa todos os elementos do último para o primeiro (do fundo para a frente)
		ElementoDeTela elementoDeTela = getUltimoElemento();
		while (elementoDeTela != null) {
			if (!elementoDeTela.isMarcadoParaRemocao()) {
				elementoDeTela.processeUmQuadro(deltaSegundos);
			}
			elementoDeTela = elementoDeTela.getElementoSeguinte();
		}

		// Os elementos devem ser desenhados apenas depois de todos terem sido processados

		// Desenha todos os elementos do último para o primeiro (do fundo para a frente)
		elementoDeTela = getUltimoElemento();
		while (elementoDeTela != null) {
			if (!elementoDeTela.isMarcadoParaRemocao()) {
				elementoDeTela.desenheUmQuadro();
			}
			elementoDeTela = elementoDeTela.getElementoSeguinte();
		}

		setProcessando(false);

		// Depois de processar o quadro temos que realizar todas as ações que estavam pendentes,
		// garantindo, assim, que a lista de objetos ativos não seja alterada durante o
		// processamento do quadro, o que poderia trazer resultados estranhos...

		AcaoPendente acaoPendente = getPrimeiraAcaoPendente();
		if (acaoPendente != null) {
			do {
				// Temos que manter uma referência para a próxima ação aqui, pois o método execute()
				// irá transformar tudo em null
				AcaoPendente temporario = acaoPendente.getAcaoSeguinte();

				acaoPendente.execute(this);

				acaoPendente = temporario;
			} while (acaoPendente != null);

			setPrimeiraAcaoPendente(null);
			setUltimaAcaoPendente(null);
		}

		AcaoPendente remocaoPendente = getPrimeiraRemocaoPendente();
		if (remocaoPendente != null) {
			do {
				// Temos que manter uma referência para a próxima ação aqui, pois o método execute()
				// irá transformar tudo em null
				AcaoPendente temporario = remocaoPendente.getAcaoSeguinte();

				remocaoPendente.execute(this);

				remocaoPendente = temporario;
			} while (remocaoPendente != null);

			setPrimeiraRemocaoPendente(null);
			setUltimaRemocaoPendente(null);
		}
	}
}
