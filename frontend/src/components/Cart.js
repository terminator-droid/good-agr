import React, { useState } from "react";

export function Cart() {
    const [cart, setCart] = useState(() => JSON.parse(localStorage.getItem("cart") || "[]"));
    const [result, setResult] = useState(null);

    const onOptimize = async () => {
        // Готовим запрос на бэкенд
        // Здесь понадобится создать корзину на бэкэнде и передать продукты!
        // Предположим cartId=1 пока что и передаём товары (можно апгрейдить до полноценной интеграции)
        let resp = await fetch(`/api/cart/1/optimize`);
        let data = await resp.json();
        setResult(data);
    };

    const clearCart = () => {
        localStorage.removeItem("cart");
        setCart([]);
        setResult(null);
    };

    return (
        <div>
            <h2>Корзина</h2>
            {cart.length === 0 && <div>Ваша корзина пуста</div>}
            {cart.map((item, idx) => (
                <div key={idx}>{item.title} — {item.shop} — {item.price}₽ × {item.quantity}</div>
            ))}
            <button onClick={onOptimize} disabled={cart.length === 0}>Где выгоднее?</button>
            <button onClick={clearCart}>Очистить корзину</button>
            {result &&
                <div style={{marginTop: 20, color: "#2563eb"}}>
                    <b>Итоги:</b><br/>
                    Заказать Самокат: {result.totalSamokat}₽<br/>
                    Заказать Лавка: {result.totalLavka}₽<br/>
                    <b>Рекомендация: {result.recommendedShop}!</b>
                </div>
            }
        </div>
    );
}
