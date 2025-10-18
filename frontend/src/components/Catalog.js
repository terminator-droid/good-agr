import React, { useEffect, useState } from "react";

export function Catalog() {
    const [products, setProducts] = useState([]);
    const [selected, setSelected] = useState({});
    const [loading, setLoading] = useState(true);
    const [cartId, setCartId] = useState(null);
    const [message, setMessage] = useState(null);

    useEffect(() => {
        // Создаем или загружаем корзину на backend
        fetch('/api/cart/create', { method: 'POST' })
            .then(res => res.json())
            .then(cart => {
                setCartId(cart.id);
                loadProducts();
            })
            .catch(err => {
                setMessage(`Ошибка при создании корзины: ${err.message}`);
                setLoading(false);
            });
    }, []);

    const loadProducts = () => {
        fetch("/api/products/comparison")
            .then(res => res.json())
            .then(products => {
                setProducts(products);
                setLoading(false);
            })
            .catch(() => {
                setMessage("Ошибка загрузки товаров");
                setLoading(false);
            });
    };

    const addToCart = async (pair) => {
        if (!cartId) {
            setMessage("Корзина не инициализирована");
            return;
        }
        const productId = pair.cheaperShop === "SAMOKAT"
            ? pair.samokatProduct.id
            : pair.lavkaProduct.id;

        try {
            const resp = await fetch(`/api/cart/${cartId}/add?productId=${productId}&quantity=1`, {
                method: "POST"
            });
            if (!resp.ok) throw new Error("Ошибка добавления в корзину");
            setSelected(prev => ({ ...prev, [pair.productName]: true }));
            setMessage(`Добавлено "${pair.productName}" в корзину.`);
        } catch (e) {
            setMessage(`Ошибка: ${e.message}`);
        }
    };

    return (
        <div>
            <h2>Каталог</h2>
            {loading && <p>Загрузка...</p>}
            {message && <p style={{ color: "red" }}>{message}</p>}
            {!loading && products.length === 0 && <p>Товары не найдены.</p>}

            {products.map(pair => (
                <div
                    key={pair.productName}
                    style={{ border: "1px solid #ddd", margin: 10, padding: 10 }}
                >
                    <b>{pair.productName}</b>
                    <div>Самокат: {pair.samokatPrice}₽ | Лавка: {pair.lavkaPrice}₽</div>
                    <div>
            <span style={{ color: "#10b981" }}>
              Выгоднее: {pair.cheaperShop} (экономия {pair.priceDifference}₽)
            </span>
                    </div>
                    <button
                        disabled={selected[pair.productName]}
                        onClick={() => addToCart(pair)}
                    >
                        {selected[pair.productName] ? "Добавлено" : "В корзину"}
                    </button>
                </div>
            ))}
        </div>
    );
}
