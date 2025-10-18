import React, { useEffect, useState } from "react";

export function Cart() {
    const [cartId, setCartId] = useState(null);
    const [cartItems, setCartItems] = useState([]);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    // При первом рендере создаем корзину на сервере
    useEffect(() => {
        async function createCart() {
            const response = await fetch('/api/cart/create', { method: 'POST' });
            const cart = await response.json();
            setCartId(cart.id);

            // Загружаем товары из localStorage и добавляем на сервер
            const localCart = JSON.parse(localStorage.getItem('cart') || '[]');
            for (const item of localCart) {
                await fetch(`/api/cart/${cart.id}/add?productId=${item.productId}&quantity=${item.quantity}`, {
                    method: 'POST',
                });
            }
            // Обновляем список с сервера
            loadCartItems(cart.id);
        }
        createCart();
    }, []);

    // Функция загрузки товаров из backend корзины
    async function loadCartItems(cartId) {
        const resp = await fetch(`/api/cart/${cartId}`);
        const cart = await resp.json();
        setCartItems(cart.items || []);
    }

    // Добавить товар в корзину локально и на сервер
    async function addProduct(productId) {
        if (!cartId) return;

        // Найдем, есть ли товар локально
        const index = cartItems.findIndex(item => item.product.id === productId);
        if (index >= 0) {
            // Увеличим количество
            const updatedItems = [...cartItems];
            updatedItems[index].quantity += 1;
            setCartItems(updatedItems);
        } else {
            // Добавляем новый товар
            const productResp = await fetch(`/api/products/${productId}`);
            const product = await productResp.json();
            setCartItems([...cartItems, { product, quantity: 1 }]);
        }
        // Отправим на сервер
        await fetch(`/api/cart/${cartId}/add?productId=${productId}&quantity=1`, { method: 'POST' });
    }

    // Удалить товар
    async function removeProduct(productId) {
        if (!cartId) return;
        const updated = cartItems.filter(item => item.product.id !== productId);
        setCartItems(updated);
        await fetch(`/api/cart/${cartId}/update?productId=${productId}&quantity=0`, { method: 'POST' });
    }

    // Оптимизировать корзину
    async function optimizeCart() {
        if (!cartId) return;
        setLoading(true);
        const resp = await fetch(`/api/cart/${cartId}/optimize`);
        const data = await resp.json();
        setResult(data);
        setLoading(false);
    }

    return (
        <div>
            <h2>Корзина</h2>
            {!cartItems.length && <div>Корзина пуста.</div>}
            {cartItems.map(({ product, quantity }) => (
                <div key={product.id} style={{ borderBottom: "1px solid #ddd", marginBottom: 10 }}>
                    <b>{product.title}</b> — {product.shop} — {product.getCurrentPrice}₽ × {quantity}
                    <button onClick={() => removeProduct(product.id)} style={{ marginLeft: 10 }}>Удалить</button>
                </div>
            ))}
            <button onClick={optimizeCart} disabled={loading || !cartItems.length}>
                {loading ? 'Оптимизация...' : 'Оптимизировать корзину'}
            </button>
            {result && (
                <div style={{ marginTop: 20, color: "#2563eb" }}>
                    <b>Результаты оптимизации:</b><br />
                    Заказать Самокат: {result.totalSamokat}₽<br />
                    Заказать Лавка: {result.totalLavka}₽<br />
                    <b>Рекомендация: {result.recommendedShop}!</b>
                </div>
            )}
        </div>
    );
}
